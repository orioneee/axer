package io.github.orioneee.processors

import io.github.orioneee.Axer
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.navigation.FlowDestinations
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
    val isEnabledRequests = AxerUIEntryPoint.availableDestinations.contains(FlowDestinations.EXCEPTIONS_FLOW)
    if (!isEnabledRequests) {
        return
    }
    val content = UNMutableNotificationContent().apply {
        setTitle("Exception Captured")
        setBody("${exception.error.name}: ${exception.error.message}")
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