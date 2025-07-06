package io.github.orioneee.unitls

import io.github.orioneee.domain.logs.LogLine

// iosMain
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.*
import kotlin.native.concurrent.freeze

internal actual object LogExporter {

    actual fun exportLogs(logs: List<LogLine>) {
        // 🔸 1. Build the .txt content
        val textContent = logs.joinToString("\n") { it.toString() }

        // 🔸 2. Persist it to a temp file
        val tempDir = NSTemporaryDirectory() ?: "/tmp/"
        val fileName = "logs_${NSDate().timeIntervalSince1970.toLong()}.txt"
        val filePath = tempDir + fileName
        val nsString: NSString = textContent as NSString
        nsString.dataUsingEncoding(NSUTF8StringEncoding)?.let { data ->
            NSFileManager.defaultManager.createFileAtPath(filePath, data, attributes = null)
        }

        // 🔸 3. Prepare a UIActivityViewController for sharing
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        // 🔸 4. Present it from the top most view controller on the main queue
        val presentBlock: () -> Unit = {
            topViewController()?.presentViewController(
                activityVC,
                animated = true,
                completion = null
            )
        }
        if (NSThread.isMainThread) {
            presentBlock()
        } else {
            dispatch_async(dispatch_get_main_queue(), presentBlock)
        }
    }

    /** Walks the view‐controller hierarchy to find the one currently visible */
    private fun topViewController(
        root: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController
    ): UIViewController? {
        val presented = root?.presentedViewController
        return when {
            presented is UINavigationController ->
                presented.visibleViewController?.let { topViewController(it) } ?: presented

            presented is UITabBarController ->
                presented.selectedViewController?.let { topViewController(it) } ?: presented

            presented != null -> topViewController(presented)
            else -> root
        }
    }
}
