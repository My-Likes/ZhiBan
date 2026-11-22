package com.zhixue.lite.feature.profile

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhixue.lite.core.ui.component.HorizontalDivider
import com.zhixue.lite.core.ui.component.Image
import com.zhixue.lite.core.ui.component.Text
import com.zhixue.lite.core.ui.theme.Theme

@Composable
fun ContactsScreen(
    onBackClick: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
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
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick)
                    .padding(8.dp)
                    .size(20.dp),
                painter = painterResource(com.zhixue.lite.core.ui.R.drawable.ic_back),
                tint = Theme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_contacts),
                color = Theme.colors.onBackground,
                style = Theme.typography.titleMedium
            )
        }
        Crossfade(targetState = viewModel.uiState) { state ->
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.zhixue.lite.core.ui.component.LoadingIndicator(color = Theme.colors.primary)
                }
                state.isError -> Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.text_contacts_error),
                        color = Theme.colors.onBackgroundVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        modifier = Modifier
                            .background(Theme.colors.surface, Theme.shapes.small)
                            .clickable { viewModel.load() }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        text = stringResource(R.string.button_retry),
                        color = Theme.colors.primary,
                        style = Theme.typography.button
                    )
                }
                else -> ContactsPanel(state)
            }
        }
    }
}

@Composable
private fun ContactsPanel(state: ContactsUiState) {
    var tab by rememberSaveable { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                modifier = Modifier
                    .clickable { tab = 0 }
                    .padding(vertical = 4.dp),
                text = stringResource(R.string.label_classmates, state.classmates.size),
                color = if (tab == 0) Theme.colors.primary else Theme.colors.onBackgroundVariant,
                style = Theme.typography.titleSmall
            )
            Text(
                modifier = Modifier
                    .clickable { tab = 1 }
                    .padding(vertical = 4.dp),
                text = stringResource(R.string.label_teachers, state.teachers.size),
                color = if (tab == 1) Theme.colors.primary else Theme.colors.onBackgroundVariant,
                style = Theme.typography.titleSmall
            )
        }
        HorizontalDivider()
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (tab == 0) {
                items(state.classmates.size) { index ->
                    val student = state.classmates[index]
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = student.name.orEmpty(),
                            color = Theme.colors.onBackground,
                            style = Theme.typography.titleSmall
                        )
                        student.clazz?.name?.let {
                            Text(
                                text = it,
                                color = Theme.colors.onBackgroundVariant,
                                style = Theme.typography.labelMedium
                            )
                        }
                    }
                    HorizontalDivider()
                }
            } else {
                items(state.teachers.size) { index ->
                    val teacher = state.teachers[index]
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = teacher.name.orEmpty(),
                            color = Theme.colors.onBackground,
                            style = Theme.typography.titleSmall
                        )
                        teacher.subjectName?.let {
                            Text(
                                text = it,
                                color = Theme.colors.onBackgroundVariant,
                                style = Theme.typography.labelMedium
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
    LaunchedEffect(Unit) { }
}
