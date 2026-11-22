package com.zhixue.lite.core.data.repository

import com.zhixue.lite.core.model.network.HomeworkAnswerResponse
import com.zhixue.lite.core.model.network.HomeworkListResponse
import com.zhixue.lite.core.network.ApiNetworkDataSource
import javax.inject.Inject

class HomeworkRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val networkDataSource: ApiNetworkDataSource
) : HomeworkRepository {

    override suspend fun getHomeworks(
        pageIndex: Int,
        completeStatus: Int,
        pageSize: Int
    ): HomeworkListResponse {
        return networkDataSource.getHomeworkList(
            pageIndex = pageIndex,
            completeStatus = completeStatus,
            pageSize = pageSize,
            token = userRepository.token
        )
    }

    override suspend fun getHomeworkAnswers(
        hwId: String,
        classId: String,
        typeCode: Int
    ): HomeworkAnswerResponse {
        return networkDataSource.getHomeworkAnswer(
            hwId = hwId,
            classId = classId,
            typeCode = typeCode,
            userId = userRepository.id
        )
    }
}
