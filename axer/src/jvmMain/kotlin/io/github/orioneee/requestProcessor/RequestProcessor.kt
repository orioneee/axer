@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.requestProcessor

import io.github.orioneee.domain.requests.Transaction

internal actual suspend fun updateNotification(requests: List<Transaction>) {}