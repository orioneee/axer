package io.github.orioneee.presentation.screens

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.presentation.navigation.database.DatabaseMobileNavigation

internal object DatabaseEntryPoint {
    @Composable
    fun DatabaseContent() {
        Surface {
            val navController = rememberNavController()
            DatabaseMobileNavigation().Host(navController)
        }
    }
}