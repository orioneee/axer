package io.github.orioneee.remote.server

import io.github.orioneee.Axer
import kotlinx.coroutines.CoroutineScope


fun Axer.runServerIfNotRunning(scope: CoroutineScope, port: Int = 55555) {}
fun Axer.stopServerIfRunning(){}