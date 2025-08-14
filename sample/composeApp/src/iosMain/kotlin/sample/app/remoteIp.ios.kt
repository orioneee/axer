package sample.app

actual fun getAxerServerIp(): String? = null
actual fun runServerIfCan(): Boolean {
    return false
}

actual fun stopServerIfCan() {
}