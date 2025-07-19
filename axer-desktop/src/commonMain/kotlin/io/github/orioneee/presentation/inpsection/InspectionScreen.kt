package io.github.orioneee.presentation.inpsection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.app_name_device
import io.github.orioneee.domain.other.DeviceData
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.selectdevice.RemoteAxerDataProvider
import org.jetbrains.compose.resources.stringResource

class InspectionScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        deviceData: DeviceData,
    ) {
        val provider =
            remember(deviceData) { RemoteAxerDataProvider("http://${deviceData.ip}:9000") }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(Res.string.app_name_device, deviceData.readableDeviceName))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBackIosNew,
                                null
                            )
                        }
                    }
                )
            },
        ) {
            Box(
                modifier = Modifier.padding(it),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                AxerUIEntryPoint().Screen(provider)
            }
        }
    }
}