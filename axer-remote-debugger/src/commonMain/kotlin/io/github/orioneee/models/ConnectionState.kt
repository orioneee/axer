package io.github.orioneee.models

data class ConnectionState(
    val isConnected: Boolean,
    val ping: Long?,
)