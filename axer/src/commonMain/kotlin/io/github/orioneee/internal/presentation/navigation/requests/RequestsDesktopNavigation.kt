package io.github.orioneee.internal.presentation.navigation.requests

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.internal.presentation.navigation.Routes
import io.github.orioneee.internal.presentation.screens.requests.details.RequestDetailsScreen

internal class RequestsDesktopNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.REQUESTS_LIST.route,
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            composable(
                Routes.REQUESTS_LIST.route
            ) {
                Surface {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select any request")
                    }
                }
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