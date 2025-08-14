package io.github.orioneee.internal.presentation.components

import io.github.orioneee.Axer
import io.github.orioneee.getServerDetails
import io.github.orioneee.runServerIfNotRunning
import io.github.orioneee.stopServerIfRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

internal actual fun getServerIp(): String? = Axer.getServerDetails()
internal actual fun startServerIfCan() = Axer.runServerIfNotRunning(applicationScope, sendInfoMessages = false)
internal actual fun stopServerIfCan() {
    Axer.stopServerIfRunning(sendInfoMessages = false)
}