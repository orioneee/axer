package com.oriooneee.axer.room

import com.oriooneee.axer.domain.database.SchemaItem

class RoomReader(
    val axerDriver: AxerBundledSQLiteDriver
) {
    val connection by lazy {
        axerDriver.driver.open(axerDriver.fileName)
    }

    enum class SQLiteColumnType(val code: Int) {
        INTEGER(1),
        FLOAT(2),
        TEXT(3),
        BLOB(4),
        NULL(5)
    }


    suspend fun getAllTables(): List<String> {
        val tables = mutableListOf<String>()
        val stmt = connection.prepare("SELECT name FROM sqlite_master WHERE type='table'")
        while (stmt.step()) {
            val name = stmt.getText(0)
            tables.add(name)
        }
        stmt.close()
        return tables
    }

    suspend fun getTableSchema(tableName: String): List<SchemaItem> {
        val stmt = connection.prepare("PRAGMA table_info($tableName)")
        val schema = mutableListOf<SchemaItem>()
        while (stmt.step()) {
            val columnName = stmt.getText(1) ?: "unknown"
            val isPrimary = stmt.getInt(5) == 1
            schema.add(
                SchemaItem(
                    name = columnName,
                    isPrimary = isPrimary
                )
            )
        }
        stmt.close()
        return schema
    }

    suspend fun getTableContent(tableName: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val statement = connection.prepare("SELECT * FROM $tableName")

        val columnCount = statement.getColumnCount()

        try {
            while (statement.step()) {
                val row = mutableListOf<String>()
                for (i in 0 until columnCount) {
                    val type = statement.getColumnType(i)
                    val value = when (type) {
                        SQLiteColumnType.INTEGER.code -> statement.getInt(i)?.toString()
                        SQLiteColumnType.FLOAT.code -> statement.getFloat(i)?.toString()
                        SQLiteColumnType.TEXT.code -> statement.getText(i)?.toString()
                        SQLiteColumnType.BLOB.code -> "ByteArray(${statement.getBlob(i)?.size ?: 0})"
                        SQLiteColumnType.NULL.code -> "NULL"
                        else -> null
                    } ?: ""
                    row.add(value)
                }
                result.add(row)
            }
        } finally {
            statement.close()
        }
        return result
    }

    suspend fun clearTable(tableName: String) {
        val stmt = connection.prepare("DELETE FROM $tableName")
        try {
            stmt.step()
        } finally {
            stmt.close()
        }
    }

    private var lastDataVersion = -1

    suspend fun hasTableChanged(): Boolean {
        val stmt = connection.prepare("PRAGMA data_version")
        val changed = try {
            stmt.step()
            val currentVersion = stmt.getInt(0) ?: -1
            if (currentVersion != lastDataVersion) {
                lastDataVersion = currentVersion
                true
            } else {
                false
            }
        } finally {
            stmt.close()
        }
        return changed
    }
}