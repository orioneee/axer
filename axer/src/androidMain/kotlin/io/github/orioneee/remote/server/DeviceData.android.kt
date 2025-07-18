package io.github.orioneee.remote.server

import android.os.Build
import io.github.orioneee.domain.other.DeviceData

actual fun getDeviceData(): DeviceData {
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val model = Build.MODEL.replaceFirstChar { it.uppercase() }
    val osVersion = Build.VERSION.RELEASE

    return DeviceData(
        osName = "Android",
        osVersion = osVersion,
        deviceModel = model,
        deviceManufacturer = manufacturer,
        deviceName = "$manufacturer $model"
    )
}
