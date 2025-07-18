package io.github.orioneee.remote.server

import io.github.orioneee.domain.other.DeviceData

actual fun getDeviceData(): DeviceData {
    return DeviceData(
        osName = "Android",
        osVersion = android.os.Build.VERSION.RELEASE,
        deviceModel = android.os.Build.MODEL,
        deviceManufacturer = android.os.Build.MANUFACTURER,
        deviceName = android.os.Build.DEVICE
    )
}