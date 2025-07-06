package io.github.orioneee.unitls

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.koin.IsolatedContext
import java.io.File

internal actual object LogExporter {

    private fun fileProviderAuthority(context: Context): String =
        context.packageName + ".fileprovider"

    actual fun exportLogs(logs: List<LogLine>) {
        val context: Context by IsolatedContext.koin.inject()

        val logFile = File(
            context.cacheDir,
            "logs_${System.currentTimeMillis()}.txt"
        ).apply { writeText(logs.joinToString("\n")) }

        val uri = FileProvider.getUriForFile(
            context,
            fileProviderAuthority(context),
            logFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, "Share logs")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
