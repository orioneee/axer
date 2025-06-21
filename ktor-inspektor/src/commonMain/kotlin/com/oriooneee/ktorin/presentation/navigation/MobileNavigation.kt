package com.oriooneee.ktorin.presentation.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.oriooneee.ktorin.presentation.BlueTheme
import com.oriooneee.ktorin.presentation.screens.details.RequestDetailsScreen
import com.oriooneee.ktorin.presentation.screens.requestList.RequestListScreen
import com.oriooneee.ktorin.presentation.screens.sandbox.SandboxScreen

class MobileNavigation {
    @Composable
    fun Host(
        navController: NavHostController
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

                        }
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