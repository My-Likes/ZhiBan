package com.zhixue.lite.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zhixue.lite.core.data.repository.ReportRepository
import com.zhixue.lite.core.domain.GetReportListUseCase
import com.zhixue.lite.core.model.data.ReportInfo
import com.zhixue.lite.core.model.network.AcademicYearResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val getReportListUseCase: GetReportListUseCase,
    private val reportRepository: ReportRepository
) : ViewModel() {

    val academicYears: MutableStateFlow<List<AcademicYearResponse>> =
        MutableStateFlow(emptyList())

    private val selectedYear = MutableStateFlow<AcademicYearResponse?>(null)

    val examList: Flow<PagingData<ReportInfo>> = selectedYear
        .flatMapLatest { year -> getReportListUseCase("exam", year) }
        .cachedIn(viewModelScope)
    val homeworkList: Flow<PagingData<ReportInfo>> =
        getReportListUseCase("homework").cachedIn(viewModelScope)

    val currentYear: MutableStateFlow<AcademicYearResponse?> = selectedYear

    init {
        viewModelScope.launch {
            runCatching { reportRepository.getAcademicYear() }
                .onSuccess { academicYears.value = it }
        }
    }

    fun selectYear(year: AcademicYearResponse?) {
        selectedYear.value = year
    }
}
