package io.github.orioneee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.internal.presentation.components.AxerTheme
import io.github.orioneee.navigation.NavigationClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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