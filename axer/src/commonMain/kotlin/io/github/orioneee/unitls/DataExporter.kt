package io.github.orioneee.unitls

import io.github.orioneee.domain.logs.LogLine

internal expect object DataExporter {
    fun exportLogs(logs: List<LogLine>)

    fun exportText(text: String, filename: String)
}