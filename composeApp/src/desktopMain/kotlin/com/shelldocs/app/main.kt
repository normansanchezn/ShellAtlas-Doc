package com.shelldocs.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val config = loadDesktopAppConfig()
    Window(
        onCloseRequest = ::exitApplication,
        title = "ShellDocs",
        state = WindowState(size = DpSize(1440.dp, 900.dp)),
    ) {
        App(config = config)
    }
}
