package com.migrely.app.architecture.ui.navigation.model

import androidx.navigation.NavController

fun interface UiDestination {
    fun navigate(navController: NavController)
}
