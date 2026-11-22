package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ExamPointsResponse(
    val examPointDTOs: List<ExamPointDTO> = emptyList(),
    val abilityDTOs: List<AbilityDTO> = emptyList()
) {
    @Serializable
    data class ExamPointDTO(
        val examPointName: String? = null,
        val score: Double = 0.0,
        val standardScore: Double = 0.0
    )

    @Serializable
    data class AbilityDTO(
        val abilityName: String? = null,
        val score: Double = 0.0,
        val standardScore: Double = 0.0
    )
}
