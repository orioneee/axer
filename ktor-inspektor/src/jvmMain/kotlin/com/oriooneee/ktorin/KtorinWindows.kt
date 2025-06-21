package com.oriooneee.ktorin

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.oriooneee.ktorin.presentation.EntryPoint
import com.oriooneee.ktorin.presentation.navigation.MobileNavigation

@Composable
fun ktorinWindows(){
    Window(
        title = "sample",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = {

        },
    ) {
        EntryPoint.Screen()
    }
}