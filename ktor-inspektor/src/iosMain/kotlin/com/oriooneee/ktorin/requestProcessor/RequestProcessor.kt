package com.oriooneee.ktorin.requestProcessor

import com.oriooneee.ktorin.domain.Transaction
import com.oriooneee.ktorin.koin.IsolatedContext
import com.oriooneee.ktorin.room.dao.RequestDao
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter


actual class RequestProcessor {
    private val dao: RequestDao by IsolatedContext.koin.inject()

    suspend fun updateNotification(firstFive: List<Transaction>) {
        val notificationText = firstFive.joinToString("\n") {
            val statusCode = it.responseStatus ?: "..."
            val method = it.method
            val path = it.path
            "$method $path - $statusCode"
        }

        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
            completionHandler = { granted, error ->
                if (granted && error == null) {
                    val content = UNMutableNotificationContent().apply {
                        setTitle("Ktorin Requests")
                        setBody(notificationText)
                    }

                    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                        timeInterval = 1.0, repeats = false
                    )

                    val request = UNNotificationRequest.requestWithIdentifier(
                        identifier = "ktorin_notification",
                        content = content,
                        trigger = trigger
                    )

                    center.addNotificationRequest(request, withCompletionHandler = null)
                }
            }
        )
    }

    actual suspend fun onSend(request: Transaction): Long {
        dao.upsert(request)
        val firstFive = dao.getFirstFive()
        updateNotification(firstFive)
        return request.id
    }

    actual suspend fun onFailed(request: Transaction) {
        dao.upsert(request)
    }

    actual suspend fun onFinished(request: Transaction) {
        dao.upsert(request)
    }
}