package io.github.orioneee.internal.domain.other

/**
 * @suppress
 */
sealed class AxerServerStatus{
    data class Started(val port: Int) : AxerServerStatus()
    data object Stopped: AxerServerStatus()
    data object NotSupported: AxerServerStatus()
}