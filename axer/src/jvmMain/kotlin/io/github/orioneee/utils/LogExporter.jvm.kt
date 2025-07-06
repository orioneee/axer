package io.github.orioneee.utils

import io.github.orioneee.domain.logs.LogLine

import java.io.File
import javax.swing.JFileChooser

internal actual object DataExporter {
    actual fun exportLogs(logs: List<LogLine>) {
        val textContent = logs.joinToString("\n") { it.toString() }
        exportText(textContent, "logs_${System.currentTimeMillis()}.txt")
    }

    actual fun exportText(text: String, filename: String) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Save As"
            selectedFile = File(filename)
        }

        val userSelection = fileChooser.showSaveDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            file.writeText(text)
        }
    }
}
