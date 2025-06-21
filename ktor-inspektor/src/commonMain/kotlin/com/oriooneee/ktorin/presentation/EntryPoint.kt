package com.oriooneee.ktorin.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.oriooneee.ktorin.presentation.navigation.DesktopNavigation
import com.oriooneee.ktorin.presentation.navigation.MobileNavigation
import com.oriooneee.ktorin.presentation.navigation.Routes
import com.oriooneee.ktorin.presentation.screens.requestList.RequestListScreen

object EntryPoint {
    @Composable
    fun Screen() {
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
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                                    if(currentRoute != Routes.REQUESTS_LIST.route) {
                                        navController.popBackStack()
                                    }
                                },
                                onClickToRequestDetails = {
                                    navController.navigate(Routes.REQUEST_DETAIL.route + "/${it.id}") {
                                        launchSingleTop = true
                                        popUpTo(Routes.REQUESTS_LIST.route)
                                    }
                                }
                            )
                        },
                        second = {
                            DesktopNavigation().Host(
                                navController,
                            )
                        }
                    )
                } else {
                    MobileNavigation().Host(navController)
                }
            }
        }
    }
}