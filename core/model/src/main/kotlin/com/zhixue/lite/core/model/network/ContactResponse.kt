package com.zhixue.lite.core.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ContactStudent(
    val id: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val mobile: String? = null,
    val code: String? = null,
    val clazz: Clazz? = null
) {
    @Serializable
    data class Clazz(
        val id: String? = null,
        val name: String? = null
    )
}

@Serializable
data class ContactTeacher(
    val id: String? = null,
    val name: String? = null,
    val mobile: String? = null,
    val subjectName: String? = null
)

@Serializable
data class ClazzsResponse(
    val clazzs: List<Clazz> = emptyList()
) {
    @Serializable
    data class Clazz(
        val id: String,
        val name: String? = null
    )
}
