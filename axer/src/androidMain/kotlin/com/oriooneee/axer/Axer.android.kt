package com.oriooneee.axer

import android.content.Context
import android.content.Intent
import com.oriooneee.AxerActivity
import com.oriooneee.axer.koin.IsolatedContext

actual fun openAxer() {
    val context: Context by IsolatedContext.koin.inject()
    val intent = Intent(context, AxerActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    context.startActivity(intent)
}