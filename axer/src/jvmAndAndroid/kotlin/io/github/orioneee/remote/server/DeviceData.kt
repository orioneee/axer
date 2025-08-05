package io.github.orioneee.remote.server

import io.github.orioneee.domain.other.DeviceData

internal expect fun getDeviceData(isReadOnly: Boolean): DeviceData