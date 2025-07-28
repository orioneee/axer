package io.github.orioneee.presentation.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import io.github.orioneee.presentation.components.SplitScreen
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.presentation.navigation.exceptions.ExceptionsDesktopNavigation
import io.github.orioneee.presentation.navigation.exceptions.ExceptionsMobileNavigation
import io.github.orioneee.presentation.screens.exceptions.ExceptionsList

internal object ExceptionEntryPoint {
    @Composable
    fun ExceptionsContent() {
        Surface {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentSelectedID = currentBackStackEntry?.arguments?.read {
                getStringOrNull("exceptionsID")?.toLongOrNull()
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                if (maxWidth > 800.dp) {
                    SplitScreen(
                        first = {
                            ExceptionsList().Screen(
                                selectedExceptionID = currentSelectedID,
                                onClear = {
                                    val currentRoute =
                                        navController.currentBackStackEntry?.destination?.route
                                    if (currentRoute != Routes.EXCEPTION_DETAIL.route) {
                                        navController.popBackStack()
                                    }
                                },
                                onClickToException = {
                                    navController.navigate(Routes.EXCEPTION_DETAIL.route + "/${it.id}") {
                                        launchSingleTop = true
                                        popUpTo(Routes.EXCEPTIONS_LIST.route)
                                    }
                                },
                            )
                        },
                        second = {
                            ExceptionsDesktopNavigation().Host(navController)
                        }
                    )
                } else {
                    ExceptionsMobileNavigation().Host(navController)
                }
            }
        }
    }
}