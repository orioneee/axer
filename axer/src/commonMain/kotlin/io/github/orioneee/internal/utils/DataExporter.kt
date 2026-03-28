package io.github.orioneee.internal.utils

import io.github.orioneee.internal.domain.logs.LogLine
import io.github.orioneee.internal.domain.requests.data.TransactionFull

internal expect object DataExporter {
    fun exportLogs(logs: List<LogLine>)

    fun exportText(text: String, filename: String)
    fun exportHar(har: HarFile)
    fun exportHarStreaming(transactions: List<TransactionFull>)
}