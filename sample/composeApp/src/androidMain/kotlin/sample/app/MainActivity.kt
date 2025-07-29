package sample.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.orioneee.Axer
import io.github.orioneee.installErrorHandler
import io.github.orioneee.presentation.components.AxerTheme
import io.github.orioneee.remote.server.runServerIfNotRunning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Axer.installErrorHandler()
        enableEdgeToEdge()
        Axer.runServerIfNotRunning(lifecycleScope)
        setContent {
            val theme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isDark = isSystemInDarkTheme()
                if (isDark) {
                    dynamicDarkColorScheme(this)
                } else {
                    dynamicLightColorScheme(this)
                }
            } else {
                val isDark = isSystemInDarkTheme()
                if (isDark) {
                    AxerTheme.dark
                } else {
                    AxerTheme.light
                }
            }
            MaterialTheme(
                colorScheme = theme
            ) {
                val notificationPermissionState =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        null
                    }
                LaunchedEffect(Unit) {
                    if (notificationPermissionState?.status?.isGranted == false) {
                        notificationPermissionState.launchPermissionRequest()
                    }
                }
                App()
            }
        }
    }
}