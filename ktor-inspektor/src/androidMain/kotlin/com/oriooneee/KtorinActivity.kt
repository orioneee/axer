package com.oriooneee

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.oriooneee.ktorin.presentation.EntryPoint

class KtorinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface {
                Log.d(
                    "KtorinActivity",
                    "onCreate called with savedInstanceState: $savedInstanceState,"
                )
                EntryPoint.Screen()
            }
        }
    }
}