package com.shelldocs.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point consumed by the SwiftUI host in iosApp. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val config = remember { loadIosAppConfig() }
    val themePrefs = remember { IosThemePreferences() }
    val sessionPrefs = remember { IosSessionPreferences() }
    App(config = config, themePrefs = themePrefs, sessionPrefs = sessionPrefs)
}
