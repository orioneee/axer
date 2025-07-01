package io.github.orioneee.room

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.orioneee.Axer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class AxerBundledSQLiteDriver private constructor() : SQLiteDriver {
    val driver = BundledSQLiteDriver()
    lateinit var fileName: String

    val queryFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        Axer.initIfCan()
    }

    override fun open(fileName: String): SQLiteConnection {
        if(!::fileName.isInitialized || this.fileName != fileName) {
            queryFlow.tryEmit(fileName)
            this.fileName = fileName
        }
        val connection = driver.open(fileName)
        return object : SQLiteConnection {
            fun isSqlQueryChangeData(sql: String): Boolean {
                return sql.startsWith("INSERT") || sql.startsWith("UPDATE") || sql.startsWith("DELETE") || sql.contains(
                    "END TRANSACTION",
                    ignoreCase = true
                )
            }

            override fun prepare(sql: String): SQLiteStatement {
                if (isSqlQueryChangeData(sql)) {
                    queryFlow.tryEmit(sql)
                }
                return connection.prepare(sql)
            }

            override fun close() {
                connection.close()
            }
        }
    }

    companion object {
        internal val isInitialized = MutableStateFlow(false)
        internal val instance: AxerBundledSQLiteDriver by lazy {
            AxerBundledSQLiteDriver()
        }

        fun getInstance(): AxerBundledSQLiteDriver {
            isInitialized.value = true
            return instance
        }
    }
}