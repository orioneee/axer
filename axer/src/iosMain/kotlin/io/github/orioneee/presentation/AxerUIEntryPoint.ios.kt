package io.github.orioneee.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSUserDefaults

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"

    actual val current: String
        get() {
            val langs = NSUserDefaults.standardUserDefaults.arrayForKey(LANG_KEY)
            return langs?.firstOrNull()?.toString() ?: "en"
        }

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        value?.let {
            NSUserDefaults.standardUserDefaults.setObject(listOf(it), forKey = LANG_KEY)
            NSUserDefaults.standardUserDefaults.synchronize()
        }
        return staticCompositionLocalOf { value ?: "en" }.provides(value ?: "en")
    }
}

