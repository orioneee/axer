package io.github.orioneee.internal.presentation.navigation.database

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import io.github.orioneee.internal.presentation.navigation.Animations
import io.github.orioneee.internal.presentation.navigation.Routes
import io.github.orioneee.internal.presentation.screens.database.TableDetails
import io.github.orioneee.internal.presentation.screens.database.allQueries.AllQueriesScreen
import io.github.orioneee.internal.presentation.screens.database.rawQuery.RawQueryScreen
import io.github.orioneee.internal.presentation.screens.database.tableList.ListTables

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
                )
            }
            composable(
                Routes.TABLE_DETAILS.route + "/{name}/{tableName}",
            ) {
                val tableName = it.arguments?.read {
                    getString("tableName")
                }
                val file = it.arguments?.read {
                    getString("name")
                }
                TableDetails().Screen(
                    name = file ?: "",
                    navController = navController,
                    tableName = tableName ?: "",
                )
            }
            composable(
                Routes.RAW_QUERY.route + "/{name}",
            ) {
                val name = it.arguments?.read {
                    getString("name")
                } ?: return@composable
                RawQueryScreen().Screen(
                    name = name,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                Routes.ALL_QUERIES.route,
            ) {
                AllQueriesScreen().Screen(navController)
            }
        }
    }
}