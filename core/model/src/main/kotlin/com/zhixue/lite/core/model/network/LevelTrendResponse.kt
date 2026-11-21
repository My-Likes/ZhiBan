package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class LevelTrendResponse(
    val list: List<LevelTrendInfo>
) {
    @Serializable
    data class LevelTrendInfo(
        // 现行接口字段为 statTotalNum（班级参加考试人数）；旧字段 totalNum 保留兼容
        val statTotalNum: Int = 0,
        val totalNum: Int = 0,
        val improveBar: ImproveBar
    ) {
        val effectiveTotalNum: Int
            get() = if (statTotalNum > 0) statTotalNum else totalNum

        @Serializable
        data class ImproveBar(
            val tag: Tag,
            val levelScale: String
        ) {
            @Serializable
            data class Tag(
                val code: String
            )
        }
    }
}