package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkListResponse(
    val list: List<Homework> = emptyList()
) {
    @Serializable
    data class Homework(
        val hwId: String,
        val hwTitle: String? = null,
        val homeWorkTypeDTO: HwTypeDTO? = null,
        val beginTime: Long = 0,
        val endTime: Long = 0,
        val createTime: Long = 0,
        val isAllowMakeup: Boolean = false,
        val classId: String? = null,
        val stuHwId: String? = null
    ) {
        @Serializable
        data class HwTypeDTO(
            val typeName: String? = null,
            val typeCode: Int = 0
        )
    }
}

@Serializable
data class HomeworkAnswerResponse(
    val sectionList: List<Section>? = null,
    val questionList: List<Question>? = null
) {
    @Serializable
    data class Section(val topicList: List<Topic> = emptyList())

    @Serializable
    data class Topic(
        val title: String? = null,
        val children: List<Child> = emptyList()
    )

    @Serializable
    data class Child(val answers: List<String> = emptyList())

    @Serializable
    data class Question(
        val questionTitle: String? = null,
        val subQuestion: List<SubQuestion> = emptyList()
    )

    @Serializable
    data class SubQuestion(val answer: List<String> = emptyList())
}

@Serializable
data class HomeworkAnswerRequest(
    val base: HwBase,
    val params: HwParams
)

@Serializable
data class HomeworkBankRequest(
    val base: HwBase,
    val params: HwBankParams
)

@Serializable
data class HwBase(
    val appId: String,
    val appVersion: String = "",
    val sysVersion: String = "v1001",
    val sysType: String = "web",
    val packageName: String = "com.iflytek.edu.hw",
    val udid: String,
    val expand: Map<String, String> = emptyMap()
)

@Serializable
data class HwParams(val hwId: String)

@Serializable
data class HwBankParams(
    val classId: String,
    val hwId: String
)
