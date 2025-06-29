package com.oriooneee.axer.presentation.navigation.database

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.oriooneee.axer.presentation.navigation.Animations
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.database.ListTables
import com.oriooneee.axer.presentation.screens.database.TableDetails
import com.oriooneee.axer.presentation.screens.database.rawQuery.RawQueryScreen

internal class DatabaseMobileNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.TABLES_LIST.route,
            exitTransition = { Animations.exitTransition },
            popEnterTransition = { Animations.popEnterTransition },
            enterTransition = { Animations.enterTransition },
            popExitTransition = { Animations.popExitTransition }
        ) {
            composable(
                Routes.TABLES_LIST.route
            ) {
                ListTables().Screen(
                    navController = navController,
                    onClickToTable = {
                        navController.navigate(
                            Routes.TABLE_DETAILS.route + "/${it.name}",
                        )
                    },
                )
            }
            composable(
                Routes.TABLE_DETAILS.route + "/{tableName}",
            ) {
                val tableName = it.arguments?.read {
                    getString("tableName")
                }
                TableDetails().Screen(
                    navController = navController,
                    tableName = tableName ?: "",
                )
            }
            composable(
                Routes.RAW_QUERY.route,
            ) {
                RawQueryScreen().Screen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}