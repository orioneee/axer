package com.oriooneee.axer.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.oriooneee.axer.koin.IsolatedContext
import com.oriooneee.axer.presentation.navigation.DesktopNavigation
import com.oriooneee.axer.presentation.navigation.MobileNavigation
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.requestList.RequestListScreen
import org.koin.compose.KoinIsolatedContext

internal object EntryPoint {
    @Composable
    fun Screen(
        onClose: (() -> Unit)?
    ) {
        KoinIsolatedContext (
            IsolatedContext.koinApp
        ){
            Surface {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentSelectedID = currentBackStackEntry?.arguments?.read {
                    getString("requestId").toLongOrNull()
                }
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (maxWidth > 800.dp) {
                        SplitScreen(
                            first = {
                                RequestListScreen().Screen(
                                    selectedRequestId = currentSelectedID,
                                    onClearRequests = {
                                        val currentRoute =
                                            navController.currentBackStackEntry?.destination?.route
                                        if (currentRoute != Routes.REQUESTS_LIST.route) {
                                            navController.popBackStack()
                                        }
                                    },
                                    onClickToRequestDetails = {
                                        navController.navigate(Routes.REQUEST_DETAIL.route + "/${it.id}") {
                                            launchSingleTop = true
                                            popUpTo(Routes.REQUESTS_LIST.route)
                                        }
                                    },
                                    onClose = onClose
                                )
                            },
                            second = {
                                DesktopNavigation().Host(
                                    navController,
                                )
                            }
                        )
                    } else {
                        MobileNavigation().Host(navController, onClose)
                    }
                }
            }
        }
    }
}