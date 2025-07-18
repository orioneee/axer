package io.github.orioneee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.presentation.selectdevice.RemoteAxerDataProvider
import io.github.orioneee.SelectDeviceScreen
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.components.AxerTheme
import io.github.orioneee.presentation.inpsection.InspectionScreen
import io.github.orioneee.presentation.navigation.Animations

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
                composable(Route.DEVICE_INSPECTION.path + "/{ip}") {
                    val ip = it.arguments?.read {
                        getString("ip")
                    }
                    val provider = remember(ip) { RemoteAxerDataProvider("http://$ip:9000") }
                    InspectionScreen().Screen(navController, provider)
                }
            }
        }
    }
}