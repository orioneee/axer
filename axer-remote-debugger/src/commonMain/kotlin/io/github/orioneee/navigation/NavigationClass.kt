package io.github.orioneee.navigation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.orioneee.internal.presentation.navigation.Animations
import io.github.orioneee.models.NavArgumentsDTO
import io.github.orioneee.presentation.inpsection.InspectionScreen
import io.github.orioneee.presentation.selectdevice.SelectDeviceScreen

class NavigationClass {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
            Surface {
                NavHost(
                    navController = navController,
                    startDestination = Route.SELECT_DEVICE.path,
                    exitTransition = { Animations.exitTransition },
                    popEnterTransition = { Animations.popEnterTransition },
                    enterTransition = { Animations.enterTransition },
                    popExitTransition = { Animations.popExitTransition }
                ) {
                    composable(Route.SELECT_DEVICE.path) {
                        SelectDeviceScreen().Screen(navController)
                    }
                    composable<NavArgumentsDTO> {
                        val route = it.toRoute<NavArgumentsDTO>().toDevice()
                        InspectionScreen().Screen(navController, route)
                    }
                }
            }
    }
}