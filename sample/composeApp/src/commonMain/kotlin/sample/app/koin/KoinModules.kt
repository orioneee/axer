package sample.app.koin

import org.koin.core.module.Module

object KoinModules {
    val module = getPlatformModule()
}

expect fun getPlatformModule(): Module