package com.zhixue.lite.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhixue.lite.core.data.repository.ReportRepository
import com.zhixue.lite.core.model.network.ContactStudent
import com.zhixue.lite.core.model.network.ContactTeacher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val classmates: List<ContactStudent> = emptyList(),
    val teachers: List<ContactTeacher> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    var uiState: ContactsUiState by mutableStateOf(ContactsUiState())
        private set

    init {
        load()
    }

    fun load() {
        uiState = ContactsUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val classmates = async { reportRepository.getClassmates() }
                val teachers = async { reportRepository.getTeachers() }
                ContactsUiState(
                    classmates = classmates.await(),
                    teachers = teachers.await(),
                    isLoading = false
                )
            }.getOrElse {
                ContactsUiState(isLoading = false, isError = true)
            }.also { uiState = it }
        }
    }
}
