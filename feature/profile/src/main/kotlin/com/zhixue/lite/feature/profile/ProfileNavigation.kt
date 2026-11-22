package com.zhixue.lite.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val PROFILE_ROUTE = "profile_route"

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_ROUTE, navOptions)
}

fun NavGraphBuilder.profileScreen(
    navigateToLogin: () -> Unit,
    navigateToModify: () -> Unit,
    navigateToContacts: () -> Unit = {}
) {
    composable(route = PROFILE_ROUTE) {
        ProfileScreen(
            navigateToLogin = navigateToLogin,
            navigateToModify = navigateToModify,
            navigateToContacts = navigateToContacts
        )
    }
}

const val CONTACTS_ROUTE = "contacts_route"

fun NavGraphBuilder.contactsScreen(
    onBackClick: () -> Unit
) {
    composable(route = CONTACTS_ROUTE) {
        ContactsScreen(
            onBackClick = onBackClick
        )
    }
}