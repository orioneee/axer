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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.Axer
import io.github.orioneee.AxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.no_available_options
import io.github.orioneee.domain.other.EnabledFeathers
import io.github.orioneee.extentions.navigateSaveState
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.components.AxerTheme
import io.github.orioneee.presentation.navigation.FlowDestinations
import io.github.orioneee.presentation.navigation.MainNavigation
import io.github.orioneee.storage.AxerSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext

val LocalAxerDataProvider = compositionLocalOf<AxerDataProvider> {
    error("AxerDataProvider not provided")
}

class AxerUIEntryPoint {
    init {
        Axer.initIfCan()
    }

    @Composable
    fun Screen(provider: AxerDataProvider) {
        KoinIsolatedContext(IsolatedContext.koinApp) {
            CompositionLocalProvider(
                LocalAxerDataProvider provides provider
            ) {
                Content()
            }
        }
    }

    @Composable
    fun Content() {
        AxerTheme.ProvideTheme {
            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStack?.destination?.route
            val dataProvider = LocalAxerDataProvider.current
            val enabledFeathers by dataProvider.getEnabledFeatures()
                .collectAsStateWithLifecycle(initialValue = EnabledFeathers.Default)

            val availableDestinations = remember(
                enabledFeathers,
            ) {
                val destinations = mutableListOf<FlowDestinations>()
                if (enabledFeathers.isEnabledRequests) {
                    destinations.add(FlowDestinations.REQUESTS_FLOW)
                }
                if (enabledFeathers.isEnabledExceptions) {
                    destinations.add(FlowDestinations.EXCEPTIONS_FLOW)
                }
                if (enabledFeathers.isEnabledLogs) {
                    destinations.add(FlowDestinations.LOG_VIEW)
                }
                if (enabledFeathers.isEnabledDatabase) {
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