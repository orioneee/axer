package com.oriooneee.axer

import com.oriooneee.axer.domain.exceptions.AxerException
import platform.Foundation.NSUUID
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

internal actual fun notifyAboutException(exception: AxerException) {
    val content = UNMutableNotificationContent().apply {
        setTitle("Exception Captured")
        setBody("${exception.shortName}: ${exception.message}")
    }

    val uuid = NSUUID().UUIDString()

    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = uuid,
        content = content,
        trigger = null // Immediate delivery
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
        // Optional: handle error
    }
}