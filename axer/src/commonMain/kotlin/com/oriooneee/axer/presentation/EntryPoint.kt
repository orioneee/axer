package com.oriooneee.axer.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.presentation.navigation.FlowDestinations
import com.oriooneee.axer.presentation.navigation.MainNavigation
import org.koin.compose.KoinIsolatedContext

internal object EntryPoint {

    @Composable
    fun Screen(
        onClose: (() -> Unit)?
    ) {
        MaterialTheme(BlueTheme) {

            KoinIsolatedContext(
                IsolatedContext.koinApp
            ) {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        FlowDestinations.entries.forEach {
                            item(
                                icon = {
                                    Icon(
                                        it.icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(it.label) },
                                selected = it.route.route == currentRoute,
                                onClick = {
                                    navController.navigate(it.route.route)
                                }
                            )
                        }
                    }
                ) {
                    MainNavigation().Host(navController)
                }
            }
        }
    }
}