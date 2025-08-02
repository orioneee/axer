package io.github.orioneee.remote.server

import io.github.orioneee.axer.generated.configs.BuildKonfig
import io.github.orioneee.domain.other.DeviceData

actual fun getDeviceData(): DeviceData {
    val os = System.getProperty("os.name").lowercase()

    return when {
        os.contains("mac") -> getMacDeviceData()
        os.contains("win") -> getWindowsDeviceData()
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> getLinuxDeviceData()
        else -> getFallbackDeviceData()
    }
}


fun runCommand(command: String): String? {
    return try {
        val parts = command.split(" ")
        val process = ProcessBuilder(parts)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getFallbackDeviceData(): DeviceData {
    return DeviceData(
        osName = System.getProperty("os.name") ?: "Unknown OS",
        osVersion = System.getProperty("os.version") ?: "Unknown Version",
        deviceModel = System.getProperty("os.arch") ?: "Unknown Model",
        deviceManufacturer = System.getProperty("java.vendor") ?: "Unknown Manufacturer",
        deviceName = System.getProperty("user.name") ?: "Unknown Device",
        axerVersion = BuildKonfig.VERSION_NAME,
        baseAppName = null,
    )
}

fun getLinuxDeviceData(): DeviceData {
    val model = runCommand("cat /sys/devices/virtual/dmi/id/product_name")?.trim()
    val manufacturer = runCommand("cat /sys/devices/virtual/dmi/id/sys_vendor")?.trim()

    return DeviceData(
        osName = runCommand("lsb_release -d")?.split(":")?.getOrNull(1)?.trim() ?: "Linux",
        osVersion = runCommand("lsb_release -r")?.split(":")?.getOrNull(1)?.trim() ?: "Unknown",
        deviceModel = model ?: "Unknown Model",
        deviceManufacturer = manufacturer ?: "Unknown Manufacturer",
        deviceName = runCommand("hostname")?.trim() ?: "Unknown",
        axerVersion = BuildKonfig.VERSION_NAME,
        baseAppName = null,
    )
}

fun getWindowsDeviceData(): DeviceData {
    return DeviceData(
        osName = System.getProperty("os.name") ?: "Windows",
        osVersion = System.getProperty("os.version") ?: "Unknown",
        deviceModel = runCommand("wmic computersystem get model")?.lines()?.getOrNull(1)?.trim()
            ?: "Unknown Model",
        deviceManufacturer = runCommand("wmic computersystem get manufacturer")?.lines()
            ?.getOrNull(1)?.trim() ?: "Unknown Manufacturer",
        deviceName = runCommand("hostname")?.trim() ?: "Unknown",
        axerVersion = BuildKonfig.VERSION_NAME,
        baseAppName = null,
    )
}

fun getMacDeviceData(): DeviceData {
    return DeviceData(
        osName = runCommand("sw_vers -productName")?.trim() ?: "macOS",
        osVersion = runCommand("sw_vers -productVersion")?.trim() ?: "Unknown",
        deviceModel = runCommand("sysctl -n hw.model")?.trim() ?: "Unknown Model",
        deviceManufacturer = "Apple Inc.",
        deviceName = runCommand("scutil --get ComputerName")?.trim() ?: "Unknown",
        axerVersion = BuildKonfig.VERSION_NAME,
        baseAppName = null,
    )
}
