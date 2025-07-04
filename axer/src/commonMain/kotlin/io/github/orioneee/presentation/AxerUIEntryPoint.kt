package io.github.orioneee.presentation

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
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.Axer
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.no_available_options
import io.github.orioneee.domain.SupportedLocales
import io.github.orioneee.extentions.navigateSaveState
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.navigation.FlowDestinations
import io.github.orioneee.presentation.navigation.MainNavigation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext

class AxerUIEntryPoint {
    companion object {
        private var customAppLocale by mutableStateOf<String?>(null)
        internal var availableDestinations by mutableStateOf(FlowDestinations.entries.toList())
        internal fun changeLocale(locale: SupportedLocales) {
            customAppLocale = locale.stringCode
        }

        internal fun configureDestinations(
            isEnabledRequests: Boolean,
            isEnableExceptions: Boolean,
            isEnabledLogs: Boolean,
            isEnableDatabase: Boolean
        ) {
            val list = mutableListOf<FlowDestinations>()
            if (isEnabledRequests) {
                list.add(FlowDestinations.REQUESTS_FLOW)
            }
            if (isEnableExceptions) {
                list.add(FlowDestinations.EXCEPTIONS_FLOW)
            }
            if (isEnabledLogs) {
                list.add(FlowDestinations.LOG_VIEW)
            }
            if (isEnableDatabase) {
                list.add(FlowDestinations.DATABASE_FLOW)
            }
            availableDestinations = list
        }
    }

    init {
        Axer.initIfCan()
    }

    @Composable
    fun Screen() {
        CompositionLocalProvider(
            LocalAppLocale provides customAppLocale,
        ) {
            key(customAppLocale) { Content() }
        }
    }

    @Composable
    fun Content() {
        MaterialTheme(BlueTheme) {
            KoinIsolatedContext(
                IsolatedContext.koinApp
            ) {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                Surface {
                    if (availableDestinations.isNotEmpty()) {
                        LaunchedEffect(availableDestinations, currentRoute) {
                            if (currentRoute == null) return@LaunchedEffect
                            val isCurrentRouteAvailable = availableDestinations.any { it.route == currentRoute }
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