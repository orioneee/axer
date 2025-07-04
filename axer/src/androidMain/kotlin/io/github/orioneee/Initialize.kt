package io.github.orioneee

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import io.github.orioneee.axer.R
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.koin.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication


@Deprecated("No need to call this function anymore, Axer is initialized automatically")
fun Axer.initialize(applicationContext: Context) {
}