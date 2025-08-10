package io.github.orioneee.internal.remote.server

import io.github.orioneee.internal.domain.other.DeviceData

internal expect fun getDeviceData(isReadOnly: Boolean): DeviceData