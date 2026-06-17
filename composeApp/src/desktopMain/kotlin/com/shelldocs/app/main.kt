package com.shelldocs.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import javax.swing.SwingUtilities

private const val SINGLE_INSTANCE_PORT = 57392

fun main() {
    // Prevent multiple JVM instances of the same app.
    // If another instance is already running, signal it to come to front and exit.
    val serverSocket = acquireSingleInstanceLock() ?: return

    val config = loadDesktopAppConfig()
    val themePrefs = DesktopThemePreferences()
    val sessionPrefs = DesktopSessionPreferences()

    application {
        Window(
            onCloseRequest = {
                serverSocket.close()
                exitApplication()
            },
            title = "ShellAtlas",
            state = WindowState(size = DpSize(1440.dp, 900.dp)),
        ) {
            // Listen for focus signals sent by subsequent launch attempts.
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    while (!serverSocket.isClosed) {
                        try {
                            serverSocket.accept().use {
                                SwingUtilities.invokeLater {
                                    window.toFront()
                                    window.requestFocus()
                                }
                            }
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            }

            App(config = config, themePrefs = themePrefs, sessionPrefs = sessionPrefs)
        }
    }
}

/**
 * Tries to bind a local socket on [SINGLE_INSTANCE_PORT].
 * - Returns the bound [ServerSocket] if this is the first instance.
 * - Sends a focus signal to the existing instance and returns null otherwise.
 */
private fun acquireSingleInstanceLock(): ServerSocket? {
    return try {
        ServerSocket(SINGLE_INSTANCE_PORT, 1, InetAddress.getByName("127.0.0.1"))
    } catch (_: Exception) {
        // Another instance owns the port — signal it to come to front.
        try { Socket("127.0.0.1", SINGLE_INSTANCE_PORT).close() } catch (_: Exception) {}
        null
    }
}
