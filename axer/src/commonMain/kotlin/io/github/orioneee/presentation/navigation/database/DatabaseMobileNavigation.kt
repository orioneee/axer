package io.github.orioneee.presentation.navigation.database

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.presentation.navigation.Animations
import io.github.orioneee.presentation.navigation.Routes
import io.github.orioneee.presentation.screens.database.ListTables
import io.github.orioneee.presentation.screens.database.TableDetails
import io.github.orioneee.presentation.screens.database.rawQuery.RawQueryScreen

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