package io.github.orioneee.internal.processors

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal object SessionManager {
    @OptIn(ExperimentalUuidApi::class)
    val sessionId by lazy {
        Uuid.random().toString()
    }
}