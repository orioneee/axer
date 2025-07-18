package io.github.orioneee.domain.other

import kotlinx.serialization.Serializable

@Serializable
data class DeviceData(
    val isAxer: Boolean = true,
    val osName: String,
    val osVersion: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val deviceName: String,
    val ip: String? = null,
){
    val readableDeviceName: String
        get() {
            val manufacturer = deviceManufacturer.orEmpty().replaceFirstChar { it.uppercase() }
            val model = deviceModel.orEmpty().replaceFirstChar { it.uppercase() }

            // Avoid Google Google Pixel 9, Apple MacBook Pro
            return if (model.startsWith(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model".trim()
            }
        }
}