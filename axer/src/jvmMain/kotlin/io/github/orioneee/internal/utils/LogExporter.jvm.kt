package io.github.orioneee.internal.utils

import io.github.orioneee.internal.domain.logs.LogLine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream

import java.io.File
import javax.swing.JFileChooser

internal actual object DataExporter {
    actual fun exportLogs(logs: List<LogLine>) {
        val textContent = logs.joinToString("\n") { it.toString() }
        exportText(textContent, "logs_${System.currentTimeMillis()}.txt")
    }

    fun chooseFile(
        filename: String
    ): File? {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Save As"
            selectedFile = File(filename)
        }

        val userSelection = fileChooser.showSaveDialog(null)
        return if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile
        } else {
            null
        }
    }

    actual fun exportText(text: String, filename: String) {
        val file = chooseFile(filename) ?: return
        file.writeText(text)
    }

    @OptIn(ExperimentalSerializationApi::class)
    actual fun exportHar(har: HarFile) {
        val file = chooseFile("har_${System.currentTimeMillis()}.har") ?: return
        try {
            file.outputStream().use { output ->
                Json { prettyPrint = true }.encodeToStream(har, output)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally, show error dialog or log error
        }
    }

}
