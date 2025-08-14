package io.github.orioneee.internal

import io.github.orioneee.internal.domain.other.AxerServerStatus
import io.github.orioneee.isAxerServerRunning
import kotlinx.coroutines.flow.Flow

actual fun getAxerServerStatus(): Flow<AxerServerStatus> = isAxerServerRunning