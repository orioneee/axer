package com.oriooneee.axer.presentation.navigation.exceptions

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.oriooneee.axer.presentation.navigation.Animations
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.exceptions.ExceptionDetails

internal class ExceptionsDesktopNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.EXCEPTIONS_LIST.route,
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            composable(
                Routes.EXCEPTIONS_LIST.route
            ) {
            }
            composable(
                Routes.EXCEPTION_DETAIL.route + "/{exceptionsID}"
            ) {
                val exceptionID = it.arguments?.read {
                    getString("exceptionsID").toLongOrNull()
                }
                if (exceptionID != null) {
                    ExceptionDetails().Screen(
                        navController = navController,
                        exceptionID = exceptionID
                    )
                }
            }
        }
    }
}