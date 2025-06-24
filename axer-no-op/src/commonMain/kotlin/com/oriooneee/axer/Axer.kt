@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.axer

import kotlinx.coroutines.Job

object Axer {
    val plugin = AxerPlugin

    fun recordException(
        throwable: Throwable,
    ) {
    }

    fun recordAsFatal(
        throwable: Throwable,
    ): Job {
        return Job()
    }
}

