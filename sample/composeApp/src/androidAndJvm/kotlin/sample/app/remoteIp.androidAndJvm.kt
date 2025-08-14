package sample.app

import io.github.orioneee.Axer
import io.github.orioneee.getServerDetails
import io.github.orioneee.runServerIfNotRunning
import io.github.orioneee.stopServerIfRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual fun getAxerServerIp() = Axer.getServerDetails()
actual fun stopServerIfCan() {
    Axer.stopServerIfRunning()
}

actual fun runServerIfCan(): Boolean {
    Axer.runServerIfNotRunning(CoroutineScope(Dispatchers.IO))
    return true
}