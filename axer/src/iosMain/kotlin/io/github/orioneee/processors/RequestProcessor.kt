package io.github.orioneee.processors

import io.github.orioneee.Axer
import io.github.orioneee.domain.requests.data.Transaction
import io.github.orioneee.storage.AxerSettings
import platform.CoreData.NSUUIDAttributeType
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import platform.UserNotifications.UNNotificationDefaultActionIdentifier

internal actual suspend fun updateNotification(requests: List<Transaction>) {
    val isEnabledRequests = AxerSettings.enableExceptionMonitor.get()
    if (!isEnabledRequests) {
        return
    }
    val notificationText = requests.joinToString("\n") {
        val statusCode = it.responseStatus ?: "..."
        val method = it.method
        val path = it.path
        "$method $path - $statusCode"
    }

    // Create notification content
    val content = UNMutableNotificationContent()
    content.setTitle("Axer")
    content.setBody(notificationText)

    val uuid = NSUUIDAttributeType.toString()

    // Create notification request
    val request = UNNotificationRequest.requestWithIdentifier(
        uuid,
        content,
        null // Trigger set to null for immediate delivery
    )

    // Set up notification center and delegate
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.delegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {
        // Handle notification presentation while the app is in the foreground
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            // Show the notification as a banner even when the app is in the foreground
            withCompletionHandler(UNNotificationPresentationOptionList)
        }

        // Handle notification tap or action
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            // Check the action identifier
            val actionIdentifier = didReceiveNotificationResponse.actionIdentifier
            if (actionIdentifier == UNNotificationDefaultActionIdentifier) {
                Axer.openAxerUI()
            }

            // Call the completion handler to finish processing
            withCompletionHandler()
        }
    }

    // Add the notification request
    center.addNotificationRequest(request) { error ->
        if (error != null) {
            println("Error adding notification: $error")
        }
    }
}