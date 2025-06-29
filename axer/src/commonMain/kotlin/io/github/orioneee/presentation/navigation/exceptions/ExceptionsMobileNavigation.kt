package io.github.orioneee.presentation.navigation.exceptions

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.presentation.navigation.Animations
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.presentation.screens.exceptions.ExceptionDetails
import io.github.orioneee.presentation.screens.exceptions.ExceptionsList

internal class ExceptionsMobileNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.EXCEPTIONS_LIST.route,
            exitTransition = { Animations.exitTransition },
            popEnterTransition = { Animations.popEnterTransition },
            enterTransition = { Animations.enterTransition },
            popExitTransition = { Animations.popExitTransition }
        ) {
            composable(
                Routes.EXCEPTIONS_LIST.route
            ) {
                ExceptionsList().Screen(
                    onClickToException = {
                        navController.navigate(Routes.EXCEPTION_DETAIL.route + "/${it.id}")
                    },
                    onClearRequests = {

                    },
                )
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