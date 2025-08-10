package io.github.orioneee

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal val AxerPlugin: ClientPlugin<AxerKtorPluginConfig> =
    createClientPlugin("Axer", ::AxerKtorPluginConfig) {
    }