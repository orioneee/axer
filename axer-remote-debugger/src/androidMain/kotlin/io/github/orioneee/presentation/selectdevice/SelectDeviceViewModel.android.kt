package io.github.orioneee.presentation.selectdevice

import io.github.orioneee.models.AdbDevice
import io.github.orioneee.models.Device
import io.ktor.client.HttpClient

actual fun DeviceScanViewModel.lookForAdbConnectAxer() {
}

actual suspend fun DeviceScanViewModel.checkIsAxerOnAdb(device: AdbDevice, port: Int, localClient: HttpClient): Result<Device> {
    return Result.failure(
        IllegalStateException("This function is not implemented for Android platform")
    )
}