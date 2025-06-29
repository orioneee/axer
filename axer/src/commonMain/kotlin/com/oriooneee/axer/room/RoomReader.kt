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

    enum class SQLiteColumnType(
        val code: Int,
        val textName: String,
    ) {
        INTEGER(1, "INTEGER"),
        FLOAT(2, "FLOAT"),
        TEXT(3, "TEXT"),
        BLOB(4, "BLOB"),
        NULL(5, "NULL");
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
            val isNotNullable = stmt.getInt(3) == 1
            val name = stmt.getColumnName(2)
            val type = stmt.getText(2)
            println("Column $2: Name = $name, Type = $type")
            schema.add(
                SchemaItem(
                    name = columnName,
                    isPrimary = isPrimary,
                    isNullable = !isNotNullable,
                    type = when (stmt.getText(2)) {
                        SQLiteColumnType.INTEGER.name -> SQLiteColumnType.INTEGER
                        SQLiteColumnType.FLOAT.name -> SQLiteColumnType.FLOAT
                        SQLiteColumnType.TEXT.name -> SQLiteColumnType.TEXT
                        SQLiteColumnType.BLOB.name -> SQLiteColumnType.BLOB
                        else -> SQLiteColumnType.NULL
                    }
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
                            value = statement.getLong(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.FLOAT.code -> RoomCell(
                            value = statement.getDouble(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.TEXT.code -> statement.getText(i)?.let { text ->
                            RoomCell(
                                value = text
                            )
                        } ?: RoomCell(
                            value = "NULL"
                        )

                        SQLiteColumnType.BLOB.code -> RoomCell(
                            value = "ByteArray(${statement.getBlob(i)?.size ?: 0})"
                        )

                        SQLiteColumnType.NULL.code -> RoomCell(
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
        cell: RoomCell?,
        type: SQLiteColumnType
    ) {
        if (cell?.value == null) {
            bindNull(index)
            return
        }
        when (type) {
            SQLiteColumnType.INTEGER -> bindLong(index, cell.value.toLong())
            SQLiteColumnType.FLOAT -> bindDouble(index, cell.value.toDouble())
            SQLiteColumnType.TEXT -> bindText(index, cell.value)
            SQLiteColumnType.NULL -> bindNull(index)
            else -> throw IllegalArgumentException("Unsupported cell type: ${type}")
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

        val primarySchemaItem = editableItem.item.schema[indexOfPrimaryKey]
        val primaryKeyValue = editableItem.item.cells[indexOfPrimaryKey]

        val stmt = connection.prepare(
            "UPDATE $tableName SET $columnName = ? WHERE ${primarySchemaItem.name} = ?"
        )

        val query = "UPDATE $tableName SET $columnName = ? WHERE ${primarySchemaItem.name} = ?"
        println("Executing query: $query with values: $newValue, $primaryKeyValue")


        try {
            stmt.bindCell(1, newValue, editableItem.schemaItem.type)
            stmt.bindCell(2, primaryKeyValue, primarySchemaItem.type)
            stmt.step()
            val columnCount = stmt.getColumnCount()
            if (columnCount != 0) {
                (0 until columnCount).forEach { i ->
                    val type = stmt.getColumnType(i)
                    println(
                        if (type == SQLiteColumnType.INTEGER.code || type == SQLiteColumnType.FLOAT.code) {
                            stmt.getLong(i) // Force step to execute
                        } else if (type == SQLiteColumnType.TEXT.code) {
                            stmt.getText(i) // Force step to execute
                        } else if (type == SQLiteColumnType.BLOB.code) {
                            stmt.getBlob(i) // Force step to execute
                        } else {
                            "NULL" // Handle NULL case
                        }
                    )
                }
            }
        } finally {
            stmt.close()
        }
    }

    suspend fun deleteRow(
        tableName: String,
        row: RowItem
    ) {
        val indexOfPrimaryKey = row.schema.indexOfFirst { it.isPrimary }

        if (indexOfPrimaryKey == -1) {
            throw IllegalStateException("No primary key found in schema")
        }

        val primaryKeySchemaItem = row.schema[indexOfPrimaryKey]
        val primaryKeyValue = row.cells[indexOfPrimaryKey]

        val stmt = connection.prepare(
            "DELETE FROM $tableName WHERE ${primaryKeySchemaItem.name} = ?"
        )

        try {
            stmt.bindCell(1, primaryKeyValue, primaryKeySchemaItem.type)
            stmt.step()
        } finally {
            stmt.close()
        }
    }

}