package com.oriooneee.axer.requestProcessor

import androidx.compose.ui.window.ComposeUIViewController
import com.oriooneee.axer.Axer
import com.oriooneee.axer.domain.requests.Transaction
import com.oriooneee.axer.presentation.EntryPoint
import platform.CoreData.NSUUIDAttributeType
import platform.UIKit.UIApplication
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

internal actual suspend fun updateNotification(requests: List<Transaction>) {
    val notificationText = requests.joinToString("\n") {
        val statusCode = it.responseStatus ?: "..."
        val method = it.method
        val path = it.path
        "$method $path - $statusCode"
    }

    val content = UNMutableNotificationContent()
    content.setTitle("Axer")
    content.setBody(notificationText)

    val uuid = NSUUIDAttributeType.toString()

    val request = UNNotificationRequest.requestWithIdentifier(
        uuid,
        content,
        null // Trigger can be set to null for immediate delivery
    )

    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.delegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            withCompletionHandler(UNNotificationPresentationOptionAlert)
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            Axer.openAxerUI()
            withCompletionHandler()
        }
    }
    center.addNotificationRequest(request) { error ->
    }
}
