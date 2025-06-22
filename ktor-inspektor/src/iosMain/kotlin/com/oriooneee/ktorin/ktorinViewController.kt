package com.oriooneee.ktorin

import androidx.compose.ui.window.ComposeUIViewController
import com.oriooneee.ktorin.presentation.EntryPoint
import platform.UIKit.UIViewController

fun KtorinViewController(): UIViewController = ComposeUIViewController { EntryPoint.Screen() }
