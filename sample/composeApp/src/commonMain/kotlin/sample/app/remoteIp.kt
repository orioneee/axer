package sample.app

expect fun getAxerServerIp(): String?

expect fun runServerIfCan(): Boolean
expect fun stopServerIfCan()