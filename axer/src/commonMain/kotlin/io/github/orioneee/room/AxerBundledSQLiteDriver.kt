package io.github.orioneee.room

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.orioneee.Axer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class AxerBundledSQLiteDriver private constructor() : SQLiteDriver {
    val driver = BundledSQLiteDriver()
    val dbFiles = mutableSetOf<String>()


    fun isSqlQueryChangeData(sql: String): Boolean {
        return sql.startsWith("INSERT") || sql.startsWith("UPDATE") || sql.startsWith("DELETE") || sql.contains(
            "END TRANSACTION",
            ignoreCase = true
        )
    }

    val allQueryFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 500,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val changeDataFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    fun onDataUpdate(){
        changeDataFlow.tryEmit("onDataUpdate")
    }


    init {
        Axer.initIfCan()
    }

    override fun open(fileName: String): SQLiteConnection {
        if (!dbFiles.contains(fileName)){
            dbFiles.add(fileName)
            changeDataFlow.tryEmit(fileName)
        }
        val connection = driver.open(fileName)
        return object : SQLiteConnection {
            override fun prepare(sql: String): SQLiteStatement {
                if (isSqlQueryChangeData(sql)) {
                    changeDataFlow.tryEmit(sql)
                }
                val originalStatement = connection.prepare(sql)
                return AxerSqlStatement(
                    originalStatement = originalStatement,
                    query = sql,
                    onStep = {
                        allQueryFlow.tryEmit(it)
                    }
                )
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