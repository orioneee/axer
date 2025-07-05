package io.github.orioneee

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.orioneee.presentation.AxerUIEntryPoint

internal class AxerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface {
                val view = LocalView.current
                val isDark = isSystemInDarkTheme()
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = Color.Transparent.toArgb()
                        window.navigationBarColor = Color.Transparent.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
                    }
                }
                AxerUIEntryPoint().Screen()
            }
        }
    }
}