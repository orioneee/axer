package com.oriooneee.axer.presentation.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.oriooneee.axer.presentation.BlueTheme
import com.oriooneee.axer.presentation.screens.details.RequestDetailsScreen
import com.oriooneee.axer.presentation.screens.requestList.RequestListScreen
import com.oriooneee.axer.presentation.screens.sandbox.SandboxScreen

internal class MobileNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
        onClose: (() -> Unit)?,
    ) {
        MaterialTheme(BlueTheme) {
            NavHost(
                navController = navController,
                startDestination = Routes.REQUESTS_LIST.route,
                exitTransition = { Animations.exitTransition },
                popEnterTransition = { Animations.popEnterTransition },
                enterTransition = { Animations.enterTransition },
                popExitTransition = { Animations.popExitTransition }
            ) {
                composable(
                    Routes.REQUESTS_LIST.route
                ) {
                    RequestListScreen().Screen(
                        onClickToRequestDetails = {
                            navController.navigate("request_detail/${it.id}")
                        },
                        onClearRequests = {

                        },
                        onClose = onClose
                    )
                }
                composable(
                    Routes.REQUEST_DETAIL.route + "/{requestId}"
                ) {
                    val requestId = it.arguments?.read {
                        getString("requestId").toLongOrNull()
                    }
                    if (requestId != null) {
                        RequestDetailsScreen().Screen(navController, requestId)
                    }
                }
                composable(
                    Routes.SANDBOX.route + "/{requestId}"
                ) {
                    val requestId = it.arguments?.read {
                        getString("requestId").toLongOrNull()
                    }
                    if (requestId != null) {
                        SandboxScreen().Screen(navController, requestId)
                    }
                }
            }
        }
    }
}