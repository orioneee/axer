package io.github.orioneee.internal

import io.github.orioneee.internal.domain.other.AxerServerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual fun getAxerServerStatus(): Flow<AxerServerStatus> = flowOf(AxerServerStatus.NotSupported)