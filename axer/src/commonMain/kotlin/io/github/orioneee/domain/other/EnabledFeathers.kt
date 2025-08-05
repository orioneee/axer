package io.github.orioneee.domain.other

import kotlinx.serialization.Serializable

@Serializable
data class EnabledFeathers(
    val isEnabledRequests: Boolean,
    val isEnabledExceptions: Boolean,
    val isEnabledLogs: Boolean,
    val isEnabledDatabase: Boolean,
    val isReadOnly: Boolean
){
    companion object {
        val Default = EnabledFeathers(
            isEnabledRequests = true,
            isEnabledExceptions = true,
            isEnabledLogs = true,
            isEnabledDatabase = true,
            isReadOnly = false
        )
    }
}