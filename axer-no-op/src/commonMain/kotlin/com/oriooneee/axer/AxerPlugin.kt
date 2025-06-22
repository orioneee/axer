package com.oriooneee.axer

import com.oriooneee.axer.config.AxerConfig
import io.ktor.client.plugins.api.createClientPlugin
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val AxerPlugin = createClientPlugin("Axer", ::AxerConfig) {}
