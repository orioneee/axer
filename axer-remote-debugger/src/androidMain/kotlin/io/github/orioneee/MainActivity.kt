package io.github.orioneee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.internal.domain.other.Theme
import io.github.orioneee.internal.presentation.components.AxerTheme
import io.github.orioneee.internal.storage.AxerSettings
import io.github.orioneee.navigation.NavigationClass

class MainActivity : ComponentActivity() {
    private fun getStatusBarStyle(
        isDark: Boolean
    ): SystemBarStyle = SystemBarStyle.run {
        val color = Color.Transparent.toArgb()
        if (isDark) {
            dark(color)
        } else {
            light(color, color)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme by AxerSettings.theme.asFlow()
                .collectAsStateWithLifecycle(Theme.FOLLOW_SYSTEM)
            val isDark = when (theme) {
                Theme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                Theme.DARK -> true
                Theme.LIGHT -> false
            }
            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = getStatusBarStyle(isDark),
                    navigationBarStyle = getStatusBarStyle(isDark)
                )
            }

            AxerTheme.ProvideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationClass().Host(rememberNavController())
                }
            }
        }
    }
}