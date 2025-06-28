package com.oriooneee.axer.presentation.screens

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
import com.oriooneee.axer.presentation.SplitScreen
import com.oriooneee.axer.presentation.navigation.Routes
import com.oriooneee.axer.presentation.navigation.database.DatabaseMobileNavigation
import com.oriooneee.axer.presentation.screens.database.ListTables

object DatabaseEntryPoint {
    @Composable
    fun DatabaseContent() {
        Surface {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentSelectedID = currentBackStackEntry?.arguments?.read {
                getStringOrNull("tableName")?.toLongOrNull()
            }
            DatabaseMobileNavigation().Host(navController)
        }
    }
}