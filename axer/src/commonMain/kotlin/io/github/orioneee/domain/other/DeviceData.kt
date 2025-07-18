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
)