package io.github.orioneee.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.koin.IsolatedContext
import java.io.File

internal actual object DataExporter {

    private fun fileProviderAuthority(context: Context): String =
        "${context.packageName}.io.orioneee.axer.provider"

    actual fun exportLogs(logs: List<LogLine>) {
        val text = logs.joinToString("\n")
        exportText(text, "logs_${System.currentTimeMillis()}.txt")
    }

    actual fun exportText(text: String, filename: String) {
        val context: Context by IsolatedContext.koin.inject()
        val logFile = File(
            context.cacheDir,
            filename
        ).apply { writeText(text) }

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
