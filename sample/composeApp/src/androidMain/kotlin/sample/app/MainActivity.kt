package sample.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.oriooneee.axer.Axer

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Axer.installAxerErrorHandler()
        enableEdgeToEdge()
        setContent {
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