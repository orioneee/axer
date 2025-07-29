package io.github.orioneee

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

fun Context.getAxerDebuggableSharedPreferences(
    name: String,
    mode: Int
): SharedPreferences {
    val preferences = getSharedPreferences(name, mode)
    return AxerPreferences(preferences, name)
}

class AxerPreferences(
    private val preferences: SharedPreferences,
    override val name: String
) : KVDebbugable, SharedPreferences {

    companion object {
        val instances = mutableListOf<KVDebbugable>()
        private const val TAG = "AxerPreferences"
    }

    init {
        instances.add(this)
        Log.d(TAG, "Initialized preferences [$name]")
    }

    override fun getAll(): Map<String, Any?> {
        Log.d(TAG, "[$name] getAll() called")
        return preferences.all
    }

    override fun edit(key: String, value: String) {
        val currentType = preferences.all[key]?.javaClass?.simpleName
        Log.d(TAG, "[$name] edit(key=\"$key\", value=\"$value\") - Current type: $currentType")

        when (currentType) {
            "String" -> preferences.edit().putString(key, value).apply()
            "Integer" -> preferences.edit().putInt(key, value.toInt()).apply()
            "Boolean" -> preferences.edit().putBoolean(key, value.toBoolean()).apply()
            "Float" -> preferences.edit().putFloat(key, value.toFloat()).apply()
            "Long" -> preferences.edit().putLong(key, value.toLong()).apply()
            else -> {
                Log.e(TAG, "[$name] Unsupported type for key: $key")
                throw IllegalArgumentException("Unsupported type: $currentType")
            }
        }
    }

    override fun remove(key: String) {
        if (preferences.contains(key)) {
            Log.d(TAG, "[$name] remove(key=\"$key\") - Key exists, removing")
            preferences.edit().remove(key).apply()
        } else {
            Log.d(TAG, "[$name] remove(key=\"$key\") - Key does not exist")
        }
    }

    override fun contains(key: String?): Boolean {
        val result = preferences.contains(key)
        Log.d(TAG, "[$name] contains(key=\"$key\") = $result")
        return result
    }

    override fun edit(): SharedPreferences.Editor {
        Log.d(TAG, "[$name] edit() called")
        return preferences.edit()
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val result = preferences.getBoolean(key, defValue)
        Log.d(TAG, "[$name] getBoolean(key=\"$key\", defValue=$defValue) = $result")
        return result
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val result = preferences.getFloat(key, defValue)
        Log.d(TAG, "[$name] getFloat(key=\"$key\", defValue=$defValue) = $result")
        return result
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val result = preferences.getInt(key, defValue)
        Log.d(TAG, "[$name] getInt(key=\"$key\", defValue=$defValue) = $result")
        return result
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val result = preferences.getLong(key, defValue)
        Log.d(TAG, "[$name] getLong(key=\"$key\", defValue=$defValue) = $result")
        return result
    }

    override fun getString(key: String?, defValue: String?): String? {
        val result = preferences.getString(key, defValue)
        Log.d(TAG, "[$name] getString(key=\"$key\", defValue=$defValue) = $result")
        return result
    }

    override fun getStringSet(key: String?, defValues: Set<String?>?): Set<String?>? {
        val result = preferences.getStringSet(key, defValues)
        Log.d(TAG, "[$name] getStringSet(key=\"$key\") = $result")
        return result
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        Log.d(TAG, "[$name] registerOnSharedPreferenceChangeListener() called")
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        Log.d(TAG, "[$name] unregisterOnSharedPreferenceChangeListener() called")
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
