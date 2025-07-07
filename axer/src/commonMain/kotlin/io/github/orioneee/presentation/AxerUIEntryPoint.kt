package io.github.orioneee.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.Axer
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.no_available_options
import io.github.orioneee.extentions.navigateSaveState
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.components.Theme
import io.github.orioneee.presentation.navigation.FlowDestinations
import io.github.orioneee.presentation.navigation.MainNavigation
import io.github.orioneee.storage.AxerSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext

class AxerUIEntryPoint {
    init {
        Axer.initIfCan()
    }

    @Composable
    fun Screen() {
        Content()
    }

    @Composable
    fun Content() {
        val isDark = isSystemInDarkTheme()
        MaterialTheme(
            if (isDark) Theme.dark
            else Theme.light
        ) {
            KoinIsolatedContext(
                IsolatedContext.koinApp
            ) {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                val isAvailableRequests by AxerSettings.enableRequestMonitor.observeAsState()
                val isAvailableExceptions by AxerSettings.enableExceptionMonitor.observeAsState()
                val isAvailableLogs by AxerSettings.enableLogMonitor.observeAsState()
                val isAvailableDatabase by AxerSettings.enableDatabaseMonitor.observeAsState()
                var availableDestinations = remember(
                    isAvailableRequests,
                    isAvailableExceptions,
                    isAvailableLogs,
                    isAvailableDatabase
                ) {
                    val destinations = mutableListOf<FlowDestinations>()
                    if (isAvailableRequests == true) {
                        destinations.add(FlowDestinations.REQUESTS_FLOW)
                    }
                    if (isAvailableExceptions == true) {
                        destinations.add(FlowDestinations.EXCEPTIONS_FLOW)
                    }
                    if (isAvailableLogs == true) {
                        destinations.add(FlowDestinations.LOG_VIEW)
                    }
                    if (isAvailableDatabase == true) {
                        destinations.add(FlowDestinations.DATABASE_FLOW)
                    }
                    destinations.toList()
                }


                Surface {
                    if (availableDestinations.isNotEmpty()) {
                        LaunchedEffect(availableDestinations, currentRoute) {
                            if (currentRoute == null) return@LaunchedEffect
                            val isCurrentRouteAvailable =
                                availableDestinations.any { it.route == currentRoute }
                            if (!isCurrentRouteAvailable) {
                                val firstAvailable = availableDestinations.first()
                                navController.navigateSaveState(firstAvailable.route)
                            }
                        }
                        NavigationSuiteScaffold(
                            navigationSuiteItems = {
                                availableDestinations.forEach {
                                    item(
                                        icon = {
                                            Icon(
                                                imageVector = it.icon,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(it.label)) },
                                        selected = it.route == currentRoute,
                                        onClick = {
                                            navController.navigateSaveState(it.route)
                                        }
                                    )
                                }
                            }
                        ) {
                            MainNavigation().Host(
                                startRoute = availableDestinations.first(),
                                navController = navController
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.no_available_options))
                        }
                    }
                }
            }
        }
    }
}