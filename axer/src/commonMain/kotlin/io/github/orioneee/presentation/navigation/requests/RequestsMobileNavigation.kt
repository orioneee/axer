package io.github.orioneee.presentation.navigation.requests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.presentation.BlueTheme
import io.github.orioneee.presentation.navigation.Animations
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.presentation.screens.requests.RequestDetailsScreen
import io.github.orioneee.presentation.screens.requests.RequestListScreen

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