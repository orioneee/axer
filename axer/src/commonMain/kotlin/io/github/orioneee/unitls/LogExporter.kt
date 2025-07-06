package io.github.orioneee.unitls

import io.github.orioneee.domain.logs.LogLine

internal expect object LogExporter {
    fun exportLogs(logs: List<LogLine>)
}