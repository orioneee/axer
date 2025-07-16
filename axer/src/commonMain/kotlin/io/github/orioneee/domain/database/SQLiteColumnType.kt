package io.github.orioneee.domain.database

enum class SQLiteColumnType(
    val code: Int,
    val textName: String,
) {
    INTEGER(1, "INTEGER"),
    FLOAT(2, "FLOAT"),
    TEXT(3, "TEXT"),
    BLOB(4, "BLOB"),
    NULL(5, "NULL");

    companion object {
        fun fromCode(code: Int): SQLiteColumnType {
            return entries.find { it.code == code } ?: NULL
        }

        fun fromTextName(name: String): SQLiteColumnType {
            return entries.find { it.textName == name } ?: NULL
        }
    }
}