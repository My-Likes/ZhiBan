package com.zhixue.lite.feature.home

import androidx.annotation.DrawableRes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.zhixue.lite.feature.profile.contactsScreen
import com.zhixue.lite.feature.profile.profileScreen
import com.zhixue.lite.feature.report.REPORT_LIST_ROUTE
import com.zhixue.lite.feature.report.reportListScreen
import com.zhixue.lite.feature.homework.homeworkScreen

const val HOME_ROUTE = "home_route"

enum class HomeDestination(
    @DrawableRes val iconRes: Int
) {
    REPORT_LIST(com.zhixue.lite.core.ui.R.drawable.ic_report),
    HOMEWORK(com.zhixue.lite.core.ui.R.drawable.ic_homework),
    PROFILE(com.zhixue.lite.core.ui.R.drawable.ic_profile)
}

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.homeGraph(
    navigateToLogin: () -> Unit,
    navigateToModify: () -> Unit,
    navigateToReportMain: (String) -> Unit,
    navigateToContacts: () -> Unit = {},
    navigateBack: () -> Unit = {}
) {
    navigation(
        route = HOME_ROUTE,
        startDestination = REPORT_LIST_ROUTE
    ) {
        reportListScreen(
            navigateToReportMain = navigateToReportMain
        )
        homeworkScreen()
        profileScreen(
            navigateToLogin = navigateToLogin,
            navigateToModify = navigateToModify,
            navigateToContacts = navigateToContacts
        )
        contactsScreen(
            onBackClick = navigateBack
        )
    }
}