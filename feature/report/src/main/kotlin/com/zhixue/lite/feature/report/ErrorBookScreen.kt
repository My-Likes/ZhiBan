package com.zhixue.lite.feature.report

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.widget.TextView
import com.zhixue.lite.core.model.network.ErrorBookResponse
import com.zhixue.lite.core.ui.component.AsyncImage
import com.zhixue.lite.core.ui.component.HorizontalDivider
import com.zhixue.lite.core.ui.component.Text
import com.zhixue.lite.core.ui.theme.Theme

@Composable
fun ErrorBookScreen(
    name: String,
    onBackClick: () -> Unit,
    viewModel: ErrorBookViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        ReportDetailTopBar(
            onBackClick = onBackClick
        )
        Crossfade(targetState = viewModel.uiState) { state ->
            when (state) {
                ErrorBookUiState.Loading -> LoadingIndicator()
                ErrorBookUiState.Error -> ErrorIndicator(
                    message = stringResource(R.string.text_error_book_empty),
                    onRetryClick = { viewModel.load() }
                )
                is ErrorBookUiState.Success -> ErrorBookPanel(
                    name = name,
                    topics = state.topics
                )
            }
        }
    }
}

@Composable
private fun ErrorBookPanel(
    name: String,
    topics: List<ErrorBookResponse.Topic>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = name,
                color = Theme.colors.onBackground,
                style = Theme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_error_book, topics.size),
                color = Theme.colors.onBackgroundVariant,
                style = Theme.typography.labelMedium
            )
        }
        items(topics.size) { index ->
            ErrorBookItem(topic = topics[index])
        }
    }
}

@Composable
private fun ErrorBookItem(topic: ErrorBookResponse.Topic) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Theme.colors.outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = "${topic.disTitleNumber.orEmpty()}  ${topic.paperName.orEmpty()}",
                color = Theme.colors.onBackground,
                style = Theme.typography.titleSmall
            )
            Text(
                text = stringResource(
                    R.string.text_error_book_score,
                    formatScore(topic.score), formatScore(topic.standardScore)
                ),
                color = Theme.colors.onBackgroundVariant,
                style = Theme.typography.labelMedium
            )
        }
        topic.contentHtml?.let {
            HtmlText(html = it, maxLines = 12)
        }
        topic.topicImgUrl?.takeIf { it.isNotBlank() }?.let { url ->
            AsyncImage(
                model = url,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
        HorizontalDivider()
        Text(
            text = stringResource(R.string.label_answer),
            color = Theme.colors.onBackgroundVariant,
            style = Theme.typography.labelMedium
        )
        topic.answerHtml?.let { HtmlText(html = it, maxLines = 8) }
        Text(
            text = stringResource(R.string.label_analysis),
            color = Theme.colors.onBackgroundVariant,
            style = Theme.typography.labelMedium
        )
        topic.analysisHtml?.let { HtmlText(html = it, maxLines = 12) }
        Text(
            text = stringResource(R.string.text_error_book_rate, topic.classScoreRate),
            color = Theme.colors.onBackgroundVariant,
            style = Theme.typography.labelMedium,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HtmlText(html: String, maxLines: Int) {
    val textColor = Theme.colors.onBackground.toArgb()
    val lineSpacing = 6f
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor)
                textSize = 14f
                this.lineSpacingExtra = lineSpacing
            }
        },
        update = { textView ->
            val spanned = Html.fromHtml(html, FROM_HTML_MODE_LEGACY)
            textView.text = spanned
        }
    )
}

@Composable
private fun LoadingIndicator() {
    com.zhixue.lite.core.ui.component.LoadingIndicator(
        color = Theme.colors.primary
    )
}

@Composable
private fun ErrorIndicator(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = Theme.colors.onBackgroundVariant,
            style = Theme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier
                .background(Theme.colors.surface, RoundedCornerShape(8.dp))
                .clickable(onClick = onRetryClick)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            text = stringResource(R.string.button_retry),
            color = Theme.colors.primary,
            style = Theme.typography.button
        )
    }
}

private fun formatScore(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
