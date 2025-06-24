package com.oriooneee.axer.presentation.screens

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
import com.oriooneee.axer.presentation.SplitScreen
import com.oriooneee.axer.presentation.navigation.exceptions.ExceptionsDesktopNavigation
import com.oriooneee.axer.presentation.navigation.exceptions.ExceptionsMobileNavigation
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.exceptions.ExceptionsList

object ExceptionEntryPoint {
    @Composable
    fun ExceptionsContent() {
        Surface {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentSelectedID = currentBackStackEntry?.arguments?.read {
                getString("exceptionsID").toLongOrNull()
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                if (maxWidth > 800.dp) {
                    SplitScreen(
                        first = {
                            ExceptionsList().Screen(
                                selectedExceptionID = currentSelectedID,
                                onClearRequests = {
                                    val currentRoute =
                                        navController.currentBackStackEntry?.destination?.route
                                    if (currentRoute != Routes.REQUESTS_LIST.route) {
                                        navController.popBackStack()
                                    }
                                },
                                onClickToException = {
                                    navController.navigate(Routes.EXCEPTION_DETAIL.route + "/${it.id}") {
                                        launchSingleTop = true
                                        popUpTo(Routes.REQUESTS_LIST.route)
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