package io.github.orioneee

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.orioneee.internal.room.AxerSQLiteConnection
import io.github.orioneee.internal.room.AxerSqlStatement
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.sample
import kotlin.time.ExperimentalTime

class AxerBundledSQLiteDriver private constructor() : SQLiteDriver {
    internal val driver = BundledSQLiteDriver()
    internal val dbFiles = mutableSetOf<String>()


    internal val allQueryFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 500,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    internal val _changeDataFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    @OptIn(FlowPreview::class)
    internal val changeDataFlow = _changeDataFlow.asSharedFlow().sample(500)

    init {
        Axer.initIfCan()
    }


    @OptIn(ExperimentalTime::class)
    override fun open(fileName: String): SQLiteConnection {
        if (!dbFiles.contains(fileName)) {
            dbFiles.add(fileName)
            _changeDataFlow.tryEmit(fileName)
        }
        val connection = driver.open(fileName)
        return AxerSQLiteConnection(
            connection = connection,
            onStep = { sql ->
                allQueryFlow.tryEmit(sql)
            },
            onChangeData = { sql ->
                _changeDataFlow.tryEmit(sql)
            }
        )
    }

    companion object {
        internal val isInitialized = MutableStateFlow(false)
        internal val instance: AxerBundledSQLiteDriver by lazy {
            AxerBundledSQLiteDriver()
        }

        fun getInstance(): SQLiteDriver {
            isInitialized.value = true
            return instance
        }
    }
}