@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.oriooneee.axer.requestProcessor

import com.oriooneee.axer.domain.requests.Transaction

internal actual suspend fun updateNotification(requests: List<Transaction>) {}