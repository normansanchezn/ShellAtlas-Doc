package com.shelldocs.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point consumed by the SwiftUI host in iosApp. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
