package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class AcademicYearResponse(
    val name: String,
    val code: String,
    val beginTime: String,
    val endTime: String
)
