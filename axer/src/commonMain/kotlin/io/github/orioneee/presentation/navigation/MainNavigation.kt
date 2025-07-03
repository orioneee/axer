package io.github.orioneee.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.presentation.navigation.database.DatabaseMobileNavigation
import io.github.orioneee.presentation.screens.ExceptionEntryPoint
import io.github.orioneee.presentation.screens.RequestsEntryPoint
import io.github.orioneee.presentation.screens.logView.LogViewScreen

internal class MainNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.REQUESTS_FLOW.route,
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            composable(Routes.REQUESTS_FLOW.route) {
                RequestsEntryPoint.RequestContent()
            }
            composable(Routes.EXCEPTIONS_FLOW.route) {
                ExceptionEntryPoint.ExceptionsContent()
            }
            composable(Routes.LOG_VIEW.route) {
                LogViewScreen().Screen(navController)
            }
            composable(Routes.DATABASE_FLOW.route) {
                DatabaseMobileNavigation().Host(rememberNavController())
            }
        }
    }
}