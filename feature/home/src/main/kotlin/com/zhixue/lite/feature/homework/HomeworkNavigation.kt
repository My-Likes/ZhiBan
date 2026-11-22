package com.zhixue.lite.feature.homework

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val HOMEWORK_ROUTE = "homework_route"

fun NavGraphBuilder.homeworkScreen() {
    composable(route = HOMEWORK_ROUTE) {
        HomeworkScreen()
    }
}
