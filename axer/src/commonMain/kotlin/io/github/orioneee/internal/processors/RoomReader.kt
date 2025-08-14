package io.github.orioneee.internal.processors

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import io.github.orioneee.internal.domain.database.DatabaseEntity
import io.github.orioneee.internal.domain.database.DatabaseWrapped
import io.github.orioneee.internal.domain.database.EditableRowItem
import io.github.orioneee.internal.domain.database.QueryResponse
import io.github.orioneee.internal.domain.database.RoomCell
import io.github.orioneee.internal.domain.database.RowItem
import io.github.orioneee.internal.domain.database.SQLiteColumnType
import io.github.orioneee.internal.domain.database.SchemaItem
import io.github.orioneee.internal.domain.database.Table
import io.github.orioneee.AxerBundledSQLiteDriver

internal class RoomReader {
    val axerDriver = AxerBundledSQLiteDriver.instance
    val connections = mutableListOf<DatabaseEntity>()

    fun updateConnections() {
        val dbFiles = axerDriver.dbFiles
        dbFiles.forEach { file ->
            if (connections.none { it.file == file }) {
                val conn = axerDriver.open(file)
                connections.add(DatabaseEntity(file, conn))
            }
        }
    }

    fun release() {
        connections.forEach { it.connection.close() }
        connections.clear()
    }

    fun getConnection(
        file: String
    ): SQLiteConnection {
        updateConnections()
        return connections.first { it.file.indexOf(file) != -1 }.connection
    }


    suspend fun getTableSize(
        file: String,
        tableName: String
    ): Int {
        val connection = getConnection(file)
        val stmt = connection.prepare("SELECT COUNT(*) FROM $tableName")
        return try {
            if (stmt.step()) {
                stmt.getLong(0).toInt()
            } else {
                0
            }
        } finally {
            stmt.close()
        }
    }


    suspend fun getAllTables(
        file: String
    ): List<Table> {
        val connection = getConnection(file)
        val tables = mutableListOf<String>()
        val stmt =
            connection.prepare("SELECT name FROM sqlite_master WHERE type='table'")
        while (stmt.step()) {
            val name = stmt.getText(0)
            tables.add(name)
        }
        stmt.close()
        return tables.map {
            val rowCount = getTableSize(file, it)
            val columnCount = getColumnCount(file, it)
            Table(
                name = it,
                rowCount = rowCount,
                columnCount = columnCount,
            )
        }
    }

    suspend fun getTablesFromAllDatabase(): List<DatabaseWrapped> {
        updateConnections()
        return connections.map {
            DatabaseWrapped(
                tables = getAllTables(it.file),
                name = it.file.substringAfterLast("/").replace(".db", ""),
            )
        }
    }

    suspend fun getColumnCount(
        file: String,
        tableName: String
    ): Int {
        val connection = getConnection(file)
        val stmt = connection.prepare("PRAGMA table_info($tableName)")
        var count = 0
        while (stmt.step()) {
            count++
        }
        stmt.close()
        return count
    }

    suspend fun getTableSchema(
        file: String,
        tableName: String
    ): List<SchemaItem> {
        val connection = getConnection(file)
        val stmt = connection.prepare("PRAGMA table_info($tableName)")
        val schema = mutableListOf<SchemaItem>()
        while (stmt.step()) {
            val columnName = stmt.getText(1) ?: "unknown"
            val isPrimary = stmt.getInt(5) == 1
            val isNotNullable = stmt.getInt(3) == 1
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

    suspend fun getTableContent(
        file: String,
        tableName: String,
        page: Int?,
        pageSize: Int?,
    ): List<List<RoomCell?>> {
        val connection = getConnection(file)
        val result = mutableListOf<List<RoomCell?>>()
        val statement = if (
            page != null && pageSize != null
        ) {
            connection.prepare("SELECT * FROM $tableName LIMIT ? OFFSET ?").apply {
                bindLong(1, pageSize.toLong())
                bindLong(2, (page * pageSize).toLong())
            }
        } else {
            connection.prepare("SELECT * FROM $tableName")
        }

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

    suspend fun clearTable(
        file: String,
        tableName: String
    ) {
        val connection = getConnection(file)
        val stmt = connection.prepare("DELETE FROM $tableName")
        try {
            stmt.step()
        } finally {
            stmt.close()
        }
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
        file: String,
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
        val connection = getConnection(file)
        val stmt = connection.prepare(
            "UPDATE $tableName SET $columnName = ? WHERE ${primarySchemaItem.name} = ?"
        )


        try {
            stmt.bindCell(1, newValue, editableItem.schemaItem.type)
            stmt.bindCell(2, primaryKeyValue, primarySchemaItem.type)
            stmt.step()
            axerDriver._changeDataFlow.emit("UPDATE $tableName SET $columnName = ? WHERE ${primarySchemaItem.name} = ?")
        } finally {
            stmt.close()
        }
    }

    suspend fun deleteRow(
        file: String,
        tableName: String,
        row: RowItem
    ) {
        val indexOfPrimaryKey = row.schema.indexOfFirst { it.isPrimary }

        if (indexOfPrimaryKey == -1) {
            throw IllegalStateException("No primary key found in schema")
        }

        val primaryKeySchemaItem = row.schema[indexOfPrimaryKey]
        val primaryKeyValue = row.cells[indexOfPrimaryKey]
        val connection = getConnection(file)
        val stmt = connection.prepare(
            "DELETE FROM $tableName WHERE ${primaryKeySchemaItem.name} = ?"
        )

        try {
            stmt.bindCell(1, primaryKeyValue, primaryKeySchemaItem.type)
            stmt.step()
            axerDriver._changeDataFlow.emit("DELETE FROM $tableName WHERE ${primaryKeySchemaItem.name} = ?")
        } finally {
            stmt.close()
        }
    }

    fun extractTableName(query: String): String? {
        val normalized =
            query.trim().replace("\n", " ").replace("\t", " ").replace(Regex("\\s+"), " ")
        val words = normalized.split(" ")

        return when {
            normalized.startsWith("INSERT", ignoreCase = true) -> {
                // INSERT INTO tableName ...
                val intoIndex = words.indexOfFirst { it.equals("INTO", ignoreCase = true) }
                if (intoIndex != -1 && intoIndex + 1 < words.size) words[intoIndex + 1] else null
            }

            normalized.startsWith("UPDATE", ignoreCase = true) -> {
                // UPDATE tableName SET ...
                if (words.size > 1) words[1] else null
            }

            normalized.startsWith("DELETE", ignoreCase = true) -> {
                // DELETE FROM tableName ...
                val fromIndex = words.indexOfFirst { it.equals("FROM", ignoreCase = true) }
                if (fromIndex != -1 && fromIndex + 1 < words.size) words[fromIndex + 1] else null
            }

            normalized.startsWith("CREATE", ignoreCase = true) -> {
                // CREATE TABLE tableName ...
                val tableIndex = words.indexOfFirst { it.equals("TABLE", ignoreCase = true) }
                if (tableIndex != -1 && tableIndex + 1 < words.size) words[tableIndex + 1] else null
            }

            normalized.startsWith("ALTER", ignoreCase = true) -> {
                // ALTER TABLE tableName ...
                val tableIndex = words.indexOfFirst { it.equals("TABLE", ignoreCase = true) }
                if (tableIndex != -1 && tableIndex + 1 < words.size) words[tableIndex + 1] else null
            }

            normalized.startsWith("DROP", ignoreCase = true) -> {
                // DROP TABLE tableName ...
                val tableIndex = words.indexOfFirst { it.equals("TABLE", ignoreCase = true) }
                if (tableIndex != -1 && tableIndex + 1 < words.size) words[tableIndex + 1] else null
            }

            else -> null
        }
    }


    suspend fun executeRawQuery(
        file: String,
        query: String
    ): QueryResponse {
        val connection = getConnection(file)
        val columns = mutableSetOf<SchemaItem>()
        val rows = mutableListOf<List<RoomCell?>>()
        val statement = connection.prepare(query)
        val columnCount = statement.getColumnCount()

        try {
            while (statement.step()) {
                val row = mutableListOf<RoomCell?>()
                for (i in 0 until columnCount) {
                    val columnName = statement.getColumnName(i)
                    val type = SQLiteColumnType.fromCode(statement.getColumnType(i))
                    if (columns.none { it.name == columnName }) {
                        columns.add(
                            SchemaItem(
                                name = columnName,
                                isPrimary = false, // Primary key info not available in files queries
                                isNullable = true, // Nullable info not available in files queries
                                type = type
                            )
                        )
                    }


                    val value = when (type) {
                        SQLiteColumnType.INTEGER -> RoomCell(
                            value = statement.getLong(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.FLOAT -> RoomCell(
                            value = statement.getDouble(i)?.toString() ?: "NULL"
                        )

                        SQLiteColumnType.TEXT -> statement.getText(i)?.let { text ->
                            RoomCell(
                                value = text
                            )
                        } ?: RoomCell(
                            value = "NULL"
                        )

                        SQLiteColumnType.BLOB -> RoomCell(
                            value = "ByteArray(${statement.getBlob(i)?.size ?: 0})"
                        )

                        SQLiteColumnType.NULL -> RoomCell(
                            value = "NULL"
                        )
                    }
                    row.add(value)
                }
                rows.add(row)
            }
        } finally {
            statement.close()
        }
        val editQueries = listOf(
            "UPDATE", "INSERT", "DELETE", "CREATE", "ALTER", "DROP"
        )
        val isEditable = editQueries.any { query.startsWith(it, ignoreCase = true) }
        val originalResponse = QueryResponse(
            schema = columns.toList(),
            rows = rows.map { row ->
                RowItem(
                    cells = row,
                    schema = columns.toList()
                )
            }
        )
        if (!isEditable) {
            return originalResponse
        } else {
            val tableName = extractTableName(query)
            if (tableName == null) {
                return originalResponse
            }
            return executeRawQuery(file, "SELECT * FROM $tableName")
        }
    }

}