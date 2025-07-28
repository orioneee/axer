package io.github.orioneee.utils

// iosMain
import io.github.orioneee.domain.logs.LogLine
import kotlinx.serialization.json.Json
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

internal actual object DataExporter {

    actual fun exportLogs(logs: List<LogLine>) {
        val textContent = logs.joinToString("\n") { it.toString() }
        exportText(textContent, "logs_${NSDate().timeIntervalSince1970}.txt")
    }

    actual fun exportText(text: String, filename: String){
        val tempDir = NSTemporaryDirectory() ?: "/tmp/"
        val fileName = filename
        val filePath = tempDir + fileName
        val nsString: NSString = text as NSString
        nsString.dataUsingEncoding(NSUTF8StringEncoding)?.let { data ->
            NSFileManager.defaultManager.createFileAtPath(filePath, data, attributes = null)
        }

        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

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

    actual fun exportHar(har: HarFile) {
        // 1. Create temporary file path
        val tempDir = NSTemporaryDirectory()
        val fileName = "har_${NSDate().timeIntervalSince1970}.har"
        val filePath = tempDir + fileName

        // 2. Serialize HAR object to JSON data
        val jsonData = try {
            val byteArray = Json { prettyPrint = true }.encodeToString(HarFile.serializer(), har)
            (byteArray as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        } catch (e: Exception) {
            println("Failed to encode HAR: $e")
            return
        }

        // 3. Write file to temporary directory
        if (jsonData != null) {
            NSFileManager.defaultManager.createFileAtPath(filePath, jsonData, attributes = null)
        } else {
            println("Failed to create HAR data")
            return
        }

        // 4. Create NSURL for file
        val fileUrl = NSURL.fileURLWithPath(filePath)

        // 5. Create UIActivityViewController for sharing
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        // 6. Present on main thread
        val presentBlock: () -> Unit = {
            topViewController()?.presentViewController(activityVC, animated = true, completion = null)
        }
        if (NSThread.isMainThread) {
            presentBlock()
        } else {
            dispatch_async(dispatch_get_main_queue(), presentBlock)
        }
    }

}
