package io.github.orioneee.unitls

import io.github.orioneee.domain.logs.LogLine

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

internal actual object LogExporter {
    actual fun exportLogs(logs: List<LogLine>) {
        val textContent = logs.joinToString("\n") { it.toString() }

        val fileChooser = JFileChooser().apply {
            dialogTitle = "Save Logs As"
            fileFilter = FileNameExtensionFilter("Text Files (*.txt)", "txt")
            selectedFile = File("logs.txt")
        }

        val userSelection = fileChooser.showSaveDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile

            if (!file.name.endsWith(".txt")) {
                file = File(file.parentFile, "${file.name}.txt")
            }

            file.writeText(textContent)
        }
    }
}
