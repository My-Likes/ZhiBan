package com.zhixue.lite.feature.homework

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhixue.lite.core.model.network.HomeworkListResponse
import com.zhixue.lite.core.ui.component.Text
import com.zhixue.lite.core.ui.theme.Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeworkScreen(
    viewModel: HomeworkViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(com.zhixue.lite.feature.home.R.string.label_homework),
                color = Theme.colors.onBackground,
                style = Theme.typography.titleLarge
            )
            Text(
                modifier = Modifier
                    .background(Theme.colors.surface, RoundedCornerShape(8.dp))
                    .clickable {
                        when (val state = viewModel.uiState) {
                            is HomeworkUiState.Success ->
                                viewModel.load(
                                    if (state.completeStatus == 0) 1 else 0
                                )
                            else -> Unit
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                text = when (val state = viewModel.uiState) {
                    is HomeworkUiState.Success ->
                        if (state.completeStatus == 0) {
                            stringResource(com.zhixue.lite.feature.home.R.string.label_show_completed)
                        } else {
                            stringResource(com.zhixue.lite.feature.home.R.string.label_show_uncompleted)
                        }
                    else -> ""
                },
                color = Theme.colors.primary,
                style = Theme.typography.labelMedium
            )
        }
        Crossfade(targetState = viewModel.uiState) { state ->
            when (state) {
                HomeworkUiState.Loading ->
                    com.zhixue.lite.core.ui.component.LoadingIndicator(
                        color = Theme.colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp)
                    )
                HomeworkUiState.Error -> HomeworkErrorPanel(
                    message = stringResource(com.zhixue.lite.feature.home.R.string.text_homework_error),
                    onRetryClick = {
                        viewModel.load(
                            (viewModel.uiState as? HomeworkUiState.Success)?.completeStatus ?: 0
                        )
                    }
                )
                is HomeworkUiState.Success -> HomeworkPanel(
                    homeworks = state.homeworks,
                    onHomeworkClick = { homework ->
                        viewModel.loadAnswers(
                            hwId = homework.hwId,
                            classId = homework.classId.orEmpty(),
                            typeCode = homework.homeWorkTypeDTO?.typeCode ?: 0
                        )
                    }
                )
            }
        }
        when (val state = viewModel.answerState) {
            HomeworkAnswerUiState.Loading -> HomeworkAnswerDialog {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.zhixue.lite.core.ui.component.LoadingIndicator(color = Theme.colors.primary)
                }
            }
            is HomeworkAnswerUiState.Success -> HomeworkAnswerDialog(onDismiss = viewModel::dismissAnswers) {
                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.answers.size) { index ->
                        val (title, answer) = state.answers[index]
                        Column {
                            if (title.isNotBlank()) {
                                Text(
                                    text = title,
                                    color = Theme.colors.onBackground,
                                    style = Theme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = stringResource(
                                    com.zhixue.lite.feature.home.R.string.text_hw_answer,
                                    answer
                                ),
                                color = Theme.colors.onBackgroundVariant
                            )
                        }
                    }
                }
            }
            HomeworkAnswerUiState.Error -> HomeworkAnswerDialog(onDismiss = viewModel::dismissAnswers) {
                Text(
                    modifier = Modifier.padding(32.dp),
                    text = stringResource(com.zhixue.lite.feature.home.R.string.text_hw_no_answer),
                    color = Theme.colors.onBackgroundVariant
                )
            }
            HomeworkAnswerUiState.Hidden -> Unit
        }
    }
}

@Composable
private fun HomeworkAnswerDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Theme.colors.background, Theme.shapes.small)
                .padding(vertical = 12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun HomeworkPanel(
    homeworks: List<HomeworkListResponse.Homework>,
    onHomeworkClick: (HomeworkListResponse.Homework) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (homeworks.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(vertical = 48.dp),
                    text = stringResource(com.zhixue.lite.feature.home.R.string.text_homework_empty),
                    color = Theme.colors.onBackgroundVariant
                )
            }
        }
        items(homeworks.size) { index ->
            HomeworkItem(homeworks[index], onHomeworkClick)
        }
    }
}

@Composable
private fun HomeworkItem(
    homework: HomeworkListResponse.Homework,
    onHomeworkClick: (HomeworkListResponse.Homework) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Theme.colors.outline, Theme.shapes.medium)
            .clickable { onHomeworkClick(homework) }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = homework.hwTitle.orEmpty(),
            color = Theme.colors.onBackground,
            style = Theme.typography.titleSmall
        )
        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = homework.homeWorkTypeDTO?.typeName.orEmpty(),
                color = Theme.colors.onBackgroundVariant,
                style = Theme.typography.labelMedium
            )
            Text(
                text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(
                    Date(homework.endTime)
                ),
                color = Theme.colors.onBackgroundVariant,
                style = Theme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun HomeworkErrorPanel(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Theme.colors.onBackgroundVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier
                .background(Theme.colors.surface, RoundedCornerShape(8.dp))
                .clickable(onClick = onRetryClick)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            text = stringResource(com.zhixue.lite.feature.home.R.string.button_retry),
            color = Theme.colors.primary,
            style = Theme.typography.button
        )
    }
}
