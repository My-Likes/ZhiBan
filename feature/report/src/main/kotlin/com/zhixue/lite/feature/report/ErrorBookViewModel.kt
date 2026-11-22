package com.zhixue.lite.feature.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhixue.lite.core.data.repository.ReportRepository
import com.zhixue.lite.core.model.network.ErrorBookResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ErrorBookUiState {
    object Loading : ErrorBookUiState
    object Error : ErrorBookUiState
    data class Success(val topics: List<ErrorBookResponse.Topic>) : ErrorBookUiState
}

@HiltViewModel
class ErrorBookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository
) : ViewModel() {

    var uiState: ErrorBookUiState by mutableStateOf(ErrorBookUiState.Loading)
        private set

    private val examId: String = checkNotNull(savedStateHandle["examId"])
    private val paperId: String = checkNotNull(savedStateHandle["paperId"])

    init {
        load()
    }

    fun load() {
        uiState = ErrorBookUiState.Loading
        viewModelScope.launch {
            uiState = runCatching {
                val response = reportRepository.getErrorBook(examId, paperId)
                val topics = response.wrongTopicAnalysis?.topicList.orEmpty()
                if (topics.isEmpty()) ErrorBookUiState.Error
                else ErrorBookUiState.Success(topics)
            }.getOrElse { ErrorBookUiState.Error }
        }
    }
}
