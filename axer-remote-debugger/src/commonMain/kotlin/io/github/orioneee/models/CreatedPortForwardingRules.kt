package io.github.orioneee.models

data class CreatedPortForwardingRules(
    val serial: String,
    val localPort: Int,
    val remotePort: Int,
)