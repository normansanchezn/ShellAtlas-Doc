package com.shelldocs.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val config = remember { loadWebAppConfig() }
        val themePrefs = remember { WebThemePreferences() }
        val sessionPrefs = remember { WebSessionPreferences() }
        App(config = config, themePrefs = themePrefs, sessionPrefs = sessionPrefs)
    }
}
