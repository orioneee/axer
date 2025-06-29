package io.github.orioneee

import io.github.orioneee.config.AxerConfig
import io.ktor.client.plugins.api.createClientPlugin
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val AxerPlugin = createClientPlugin("Axer", ::AxerConfig) {

}