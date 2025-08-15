package io.github.orioneee.internal.room

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

internal class AxerSQLiteConnection(
    private val connection: SQLiteConnection,
    private val onStep: (String) -> Unit,
    private val onChangeData: (String) -> Unit,
) : SQLiteConnection {
    internal fun isSqlQueryChangeData(sql: String): Boolean {
        return sql.startsWith("INSERT") || sql.startsWith("UPDATE") || sql.startsWith("DELETE") || sql.contains(
            "END TRANSACTION",
            ignoreCase = true
        )
    }

    override fun prepare(sql: String): SQLiteStatement {
        if (isSqlQueryChangeData(sql)) {
            onChangeData(sql)
        }
        val originalStatement = connection.prepare(sql)
        return AxerSqlStatement(
            originalStatement = originalStatement,
            query = sql,
            onStep = {
                onStep(it)
            }
        )
    }

    override fun close() {
        connection.close()
    }
}