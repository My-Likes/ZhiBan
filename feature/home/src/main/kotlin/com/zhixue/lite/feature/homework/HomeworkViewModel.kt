package com.zhixue.lite.feature.homework

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhixue.lite.core.data.repository.HomeworkRepository
import com.zhixue.lite.core.model.network.HomeworkAnswerResponse
import com.zhixue.lite.core.model.network.HomeworkListResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeworkUiState {
    object Loading : HomeworkUiState
    object Error : HomeworkUiState
    data class Success(
        val homeworks: List<HomeworkListResponse.Homework>,
        val completeStatus: Int
    ) : HomeworkUiState
}

sealed interface HomeworkAnswerUiState {
    object Hidden : HomeworkAnswerUiState
    object Loading : HomeworkAnswerUiState
    object Error : HomeworkAnswerUiState
    data class Success(val answers: List<Pair<String, String>>) : HomeworkAnswerUiState
}

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {

    var uiState: HomeworkUiState by mutableStateOf(HomeworkUiState.Loading)
        private set

    var answerState: HomeworkAnswerUiState by mutableStateOf(HomeworkAnswerUiState.Hidden)
        private set

    init {
        load(completeStatus = 0)
    }

    fun load(completeStatus: Int) {
        uiState = HomeworkUiState.Loading
        viewModelScope.launch {
            uiState = runCatching {
                val response = homeworkRepository.getHomeworks(completeStatus = completeStatus)
                HomeworkUiState.Success(response.list, completeStatus)
            }.getOrElse { HomeworkUiState.Error }
        }
    }

    fun loadAnswers(hwId: String, classId: String, typeCode: Int) {
        answerState = HomeworkAnswerUiState.Loading
        viewModelScope.launch {
            answerState = runCatching {
                val response = homeworkRepository.getHomeworkAnswers(hwId, classId, typeCode)
                val answers = buildList {
                    response.sectionList?.forEach { section ->
                        section.topicList.forEach { topic ->
                            topic.children.forEach { child ->
                                add((topic.title ?: "") to child.answers.joinToString(" "))
                            }
                        }
                    }
                    response.questionList?.forEach { question ->
                        add(
                            (question.questionTitle ?: "") to
                                question.subQuestion.joinToString(" ") { it.answer.joinToString(" ") }
                        )
                    }
                }
                if (answers.isEmpty()) HomeworkAnswerUiState.Error
                else HomeworkAnswerUiState.Success(answers)
            }.getOrElse { HomeworkAnswerUiState.Error }
        }
    }

    fun dismissAnswers() {
        answerState = HomeworkAnswerUiState.Hidden
    }
}
