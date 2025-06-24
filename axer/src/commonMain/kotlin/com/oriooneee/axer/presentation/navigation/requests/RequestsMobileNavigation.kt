package com.oriooneee.axer.presentation.navigation.requests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.oriooneee.axer.presentation.BlueTheme
import com.oriooneee.axer.presentation.navigation.Animations
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.requests.RequestDetailsScreen
import com.oriooneee.axer.presentation.screens.requests.RequestListScreen

internal class RequestsMobileNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
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
            }
        }
    }
}