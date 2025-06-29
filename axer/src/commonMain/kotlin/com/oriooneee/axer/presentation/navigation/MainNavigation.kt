package com.oriooneee.axer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oriooneee.axer.presentation.screens.DatabaseEntryPoint
import com.oriooneee.axer.presentation.screens.ExceptionEntryPoint
import com.oriooneee.axer.presentation.screens.RequestsEntryPoint

internal class MainNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.REQUESTS_FLOW.route,
        ) {
            composable(Routes.REQUESTS_FLOW.route) {
                RequestsEntryPoint.RequestContent()
            }
            composable(Routes.EXCEPTIONS_FLOW.route) {
                ExceptionEntryPoint.ExceptionsContent()
            }
            composable(Routes.DATABASE_FLOW.route) {
                DatabaseEntryPoint.DatabaseContent()
            }
        }
    }
}