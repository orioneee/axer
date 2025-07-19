package io.github.orioneee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.savedstate.read
import io.github.orioneee.presentation.selectdevice.SelectDeviceScreen
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.presentation.components.AxerTheme
import io.github.orioneee.presentation.inpsection.InspectionScreen
import io.github.orioneee.presentation.navigation.Animations
import io.github.orioneee.RemoteAxerDataProvider

class NavigationClass {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        AxerTheme.ProvideTheme {
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
                composable<DeviceData> {
                    val route = it.toRoute<DeviceData>()
                    InspectionScreen().Screen(navController, route)
                }
            }
        }
    }
}