package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ErrorBookResponse(
    val wrongTopicAnalysis: WrongTopicAnalysis? = null
) {
    @Serializable
    data class WrongTopicAnalysis(
        val topicList: List<Topic> = emptyList()
    )

    @Serializable
    data class Topic(
        val disTitleNumber: String? = null,
        val contentHtml: String? = null,
        val answerHtml: String? = null,
        val analysisHtml: String? = null,
        val standardAnswer: String? = null,
        val score: Double = 0.0,
        val standardScore: Double = 0.0,
        val beCorrect: Boolean = false,
        val classScoreRate: Double = 0.0,
        val difficultyValue: Int = 0,
        val answerType: String? = null,
        val paperId: String? = null,
        val paperName: String? = null,
        val topicId: String? = null,
        val topicImgUrl: String? = null,
        val topicAnalysisImgUrl: String? = null,
        val topicSourcePaperName: String? = null
    )
}
