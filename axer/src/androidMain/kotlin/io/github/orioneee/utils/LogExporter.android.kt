package io.github.orioneee.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.orioneee.domain.logs.LogLine
import io.github.orioneee.koin.IsolatedContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
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

    @OptIn(ExperimentalSerializationApi::class)
    actual fun exportHar(har: HarFile) {
        val context: Context by IsolatedContext.koin.inject()

        // Create HAR file in cache directory
        val harFile = File(
            context.cacheDir,
            "har_${System.currentTimeMillis()}.har"
        )

        // Serialize HAR object to file
        harFile.outputStream().use { output ->
            Json { prettyPrint = true }.encodeToStream(har, output)
        }

        // Get URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            fileProviderAuthority(context),
            harFile
        )

        // Create send intent
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json" // HAR is JSON
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start chooser
        context.startActivity(
            Intent.createChooser(sendIntent, "Share HAR file")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

}
