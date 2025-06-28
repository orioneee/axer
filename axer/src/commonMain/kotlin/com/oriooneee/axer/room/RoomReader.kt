package com.oriooneee.axer.room

import androidx.sqlite.SQLiteStatement
import com.oriooneee.axer.domain.database.EditableRowItem
import com.oriooneee.axer.domain.database.RoomCell
import com.oriooneee.axer.domain.database.RowItem
import com.oriooneee.axer.domain.database.SchemaItem

internal class RoomReader(
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
        return tables.filter {
            it != "sqlite_sequence" && it != "android_metadata" && it != "room_master_table"
        }
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

    suspend fun getTableContent(tableName: String): List<List<RoomCell?>> {
        val result = mutableListOf<List<RoomCell?>>()
        val statement = connection.prepare("SELECT * FROM $tableName")

        val columnCount = statement.getColumnCount()

        try {
            while (statement.step()) {
                val row = mutableListOf<RoomCell?>()
                for (i in 0 until columnCount) {
                    val type = statement.getColumnType(i)
                    val value = when (type) {
                        SQLiteColumnType.INTEGER.code -> RoomCell(
                            type = SQLiteColumnType.INTEGER,
                            value = statement.getLong(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.FLOAT.code -> RoomCell(
                            type = SQLiteColumnType.FLOAT,
                            value = statement.getDouble(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.TEXT.code -> statement.getText(i)?.let { text ->
                            RoomCell(
                                type = SQLiteColumnType.TEXT,
                                value = text
                            )
                        } ?: RoomCell(
                            type = SQLiteColumnType.NULL,
                            value = "NULL"
                        )

                        SQLiteColumnType.BLOB.code -> RoomCell(
                            type = SQLiteColumnType.BLOB,
                            value = "ByteArray(${statement.getBlob(i)?.size ?: 0})"
                        )

                        SQLiteColumnType.NULL.code -> RoomCell(
                            type = SQLiteColumnType.NULL,
                            value = "NULL"
                        )

                        else -> null
                    }
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

    fun SQLiteStatement.bindCell(
        index: Int,
        cell: RoomCell?
    ) {
        when (cell?.type) {
            SQLiteColumnType.INTEGER -> bindInt(index, cell.value.toInt())
            SQLiteColumnType.FLOAT -> bindDouble(index, cell.value.toDouble())
            SQLiteColumnType.TEXT -> bindText(index, cell.value)
//            SQLiteColumnType.BLOB -> bindBlob(index, cell.value.toByteArray())
            SQLiteColumnType.NULL -> bindNull(index)
            else -> throw IllegalArgumentException("Unsupported cell type: ${cell?.type}")
        }
    }

    suspend fun updateCell(
        tableName: String,
        editableItem: EditableRowItem
    ) {
        val columnName = editableItem.item.schema[editableItem.selectedColumnIndex].name
        val newValue = editableItem.editedValue
        val indexOfPrimaryKey = editableItem.item.schema.indexOfFirst { it.isPrimary }

        if (indexOfPrimaryKey == -1) {
            throw IllegalStateException("No primary key found in schema")
        }

        val primaryKeyColumnName = editableItem.item.schema[indexOfPrimaryKey].name
        val primaryKeyValue = editableItem.item.cells[indexOfPrimaryKey]

        val stmt = connection.prepare(
            "UPDATE $tableName SET $columnName = ? WHERE $primaryKeyColumnName = ?"
        )

        try {
            stmt.bindCell(1, newValue)
            stmt.bindCell(2, primaryKeyValue)
            stmt.step()
        } finally {
            stmt.close()
        }
    }

    suspend fun deleteRow(
        tableName: String,
        row: RowItem
    ){
        val indexOfPrimaryKey = row.schema.indexOfFirst { it.isPrimary }

        if (indexOfPrimaryKey == -1) {
            throw IllegalStateException("No primary key found in schema")
        }

        val primaryKeyColumnName = row.schema[indexOfPrimaryKey].name
        val primaryKeyValue = row.cells[indexOfPrimaryKey]

        val stmt = connection.prepare(
            "DELETE FROM $tableName WHERE $primaryKeyColumnName = ?"
        )

        try {
            stmt.bindCell(1, primaryKeyValue)
            stmt.step()
        } finally {
            stmt.close()
        }
    }

}