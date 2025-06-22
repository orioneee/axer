package com.oriooneee.ktorin

import platform.Foundation.NSUserDefaults


object NotificationHelper {
    var shouldOpenKtorinView: Boolean
        get() = NSUserDefaults.standardUserDefaults.boolForKey("shouldOpenKtorinView")
        set(value) = NSUserDefaults.standardUserDefaults.setBool(
            value,
            forKey = "shouldOpenKtorinView"
        )
}
