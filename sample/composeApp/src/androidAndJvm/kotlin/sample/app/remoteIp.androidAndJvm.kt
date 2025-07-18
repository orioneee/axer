package sample.app

import io.github.orioneee.Axer
import io.github.orioneee.getServerDetails
import io.github.orioneee.remote.server.runServerIfNotRunning

actual fun getAxerServerIp() = Axer.getServerDetails()
actual fun runServerIfAvailable() {
    Axer.runServerIfNotRunning()
}