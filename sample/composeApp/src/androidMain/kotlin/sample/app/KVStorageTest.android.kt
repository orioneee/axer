package sample.app

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import io.github.orioneee.getAxerDebuggableSharedPreferences
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject

actual val isSupportKVTest: Boolean = true

actual fun testKVStorage() {
    val context: Context by inject(Context::class.java)

    val preferences = context.getAxerDebuggableSharedPreferences(
        name = "test_preferences",
        mode = Context.MODE_PRIVATE
    )

    val testData = mapOf(
        "stringKey" to "hello",
        "intKey" to "123",
        "boolKey" to "true",
        "floatKey" to "3.14",
        "longKey" to "10000000000"
    )

    // 1) Clear existing keys to start fresh
    for (key in preferences.all.keys) {
        preferences.edit {
            remove(key)
        }
    }
    Log.d("TestKVStorage", "Cleared all existing keys")

    // 2) Add all test data using edit(key, value)
    testData.forEach { (key, value) ->
        try {
            preferences.edit {
                putString(key, value)
            }
            Log.d("TestKVStorage", "Added key=$key value=$value")
        } catch (e: Exception) {
            Log.e("TestKVStorage", "Failed to add key=$key value=$value: ${e.message}")
        }
    }

    // 3) Verify all keys exist and print values using get<Type>
    testData.forEach { (key, value) ->
        if (!preferences.contains(key)) {
            Log.e("TestKVStorage", "Key $key missing after add")
        } else {
            val expectedType = preferences.all[key]?.javaClass?.simpleName
            val readValue = when (expectedType) {
                "String" -> preferences.getString(key, null)
                "Integer" -> preferences.getInt(key, -1).toString()
                "Boolean" -> preferences.getBoolean(key, false).toString()
                "Float" -> preferences.getFloat(key, -1f).toString()
                "Long" -> preferences.getLong(key, -1L).toString()
                else -> "Unknown type"
            }
            Log.d("TestKVStorage", "Key=$key ExpectedValue=${value} ReadValue=$readValue")
        }
    }

    // 4) Remove a key and verify removal
    val removeKey = "stringKey"
    preferences.edit {
        remove(removeKey)
        Log.d("TestKVStorage", "Removed key $removeKey")
    }
    if (!preferences.contains(removeKey)) {
        Log.d("TestKVStorage", "Successfully removed key $removeKey")
    } else {
        Log.e("TestKVStorage", "Failed to remove key $removeKey")
    }


    Log.d("TestKVStorage", "Test completed.")
}