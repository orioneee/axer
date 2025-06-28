package com.oriooneee.axer.presentation.navigation.database

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.screens.database.ListTables
import com.oriooneee.axer.presentation.screens.database.TableDetails

internal class DatabaseDesktopNavigation {
    @Composable
    fun Host(
        navController: NavHostController,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.TABLES_LIST.route,
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            enterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            composable(
                Routes.TABLES_LIST.route
            ) {
            }
            composable(
                Routes.TABLE_DETAILS.route + "/{tableName}",
            ) {
                val tableName = it.arguments?.read {
                    getString("tableName")
                }
                TableDetails().Screen(
                    tableName = tableName ?: "",
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}