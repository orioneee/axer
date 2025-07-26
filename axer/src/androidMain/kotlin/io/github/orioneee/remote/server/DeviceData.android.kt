package io.github.orioneee.remote.server

import android.os.Build
import com.russhwolf.settings.BuildConfig
import io.github.orioneee.axer.generated.configs.BuildKonfig
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
        deviceName = "$manufacturer $model",
        axerVersion = BuildKonfig.VERSION_NAME,
    )
}
