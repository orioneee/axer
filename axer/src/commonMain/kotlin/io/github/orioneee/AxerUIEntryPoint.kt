package io.github.orioneee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import io.github.orioneee.internal.AxerDataProvider
import io.github.orioneee.internal.LocalAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.no_available_options
import io.github.orioneee.internal.domain.other.EnabledFeathers
import io.github.orioneee.internal.extentions.navigateSaveState
import io.github.orioneee.internal.extentions.successData
import io.github.orioneee.internal.koin.IsolatedContext
import io.github.orioneee.internal.presentation.components.AxerTheme
import io.github.orioneee.internal.presentation.navigation.FlowDestinations
import io.github.orioneee.internal.presentation.navigation.MainNavigation
import io.github.orioneee.internal.room.AxerDatabase
import io.github.orioneee.internal.snackbarProcessor.SnackBarEvent
import io.github.orioneee.internal.snackbarProcessor.snackbarEvents
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi

internal val LocalAxerDataProvider = compositionLocalOf<AxerDataProvider> {
    error("AxerDataProvider not provided")
}

class AxerUIEntryPoint {
    init {
        Axer.initIfCan()
    }


    /**
     * @suppress
     */
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
    fun Screen() {
        KoinIsolatedContext(IsolatedContext.koinApp) {
            val database: AxerDatabase = koinInject()
            val provider = LocalAxerDataProvider(database)
            Screen(provider)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    internal fun Content() {
        AxerTheme.ProvideTheme {
            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStack?.destination?.route
            val dataProvider = LocalAxerDataProvider.current
            val enabledFeathers by dataProvider.getEnabledFeatures().successData()
                .filterNotNull()
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
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) {
                snackbarEvents
                    .onEach {
                        println("Snackbar event: $it")
                        when (it) {
                            is SnackBarEvent.Message -> {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = it.text,
                                    duration = it.duration,
                                    withDismissAction = true
                                )
                            }

                            is SnackBarEvent.Dismiss -> {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    }
                    .launchIn(this)
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
                        Scaffold(
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            snackbarHost = {
                                SnackbarHost(snackbarHostState)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it),
                            ) {
                                MainNavigation().Host(
                                    startRoute = availableDestinations.first(),
                                    navController = navController
                                )
                            }
                        }
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