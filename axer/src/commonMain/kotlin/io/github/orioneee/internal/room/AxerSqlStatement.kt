package io.github.orioneee.internal.room

import androidx.sqlite.SQLiteStatement

internal class AxerSqlStatement(
    private val originalStatement: SQLiteStatement,
    query: String,
    private val onStep: (String) -> Unit,
) : SQLiteStatement by originalStatement {
    private var bindedString = query

    fun bindValue(index: Int, value: Any?) {
        val indexOfReplaced = bindedString.indexOf("?", index - 1).takeIf { it >= 0 } ?: return
        bindedString = bindedString.replaceRange(
            indexOfReplaced, indexOfReplaced + 1, when (value) {
                null -> "NULL"
                is String -> "'${value.replace("'", "''")}'"
                is Long -> value.toString()
                is Double -> value.toString()
                is ByteArray -> "BLOB"
                else -> "Unsupported type: ${value::class.simpleName}"
            }
        )
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        bindValue(index, value)
        originalStatement.bindBlob(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        bindValue(index, value)
        originalStatement.bindDouble(index, value)
    }

    override fun bindLong(index: Int, value: Long) {
        bindValue(index, value)
        originalStatement.bindLong(index, value)
    }

    override fun bindText(index: Int, value: String) {
        bindValue(index, value)
        originalStatement.bindText(index, value)
    }

    override fun bindNull(index: Int) {
        bindValue(index, null)
        originalStatement.bindNull(index)
    }

    override fun close() {
        onStep(bindedString)
        originalStatement.close()
    }
}