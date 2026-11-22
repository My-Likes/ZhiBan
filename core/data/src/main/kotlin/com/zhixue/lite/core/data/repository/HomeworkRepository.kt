package com.zhixue.lite.core.data.repository

import com.zhixue.lite.core.model.network.HomeworkAnswerResponse
import com.zhixue.lite.core.model.network.HomeworkListResponse

interface HomeworkRepository {

    suspend fun getHomeworks(
        pageIndex: Int = 1,
        completeStatus: Int = 0,
        pageSize: Int = 20
    ): HomeworkListResponse

    suspend fun getHomeworkAnswers(
        hwId: String,
        classId: String,
        typeCode: Int
    ): HomeworkAnswerResponse
}
