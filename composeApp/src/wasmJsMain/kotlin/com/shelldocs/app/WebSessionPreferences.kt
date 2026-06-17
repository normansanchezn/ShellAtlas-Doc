package com.shelldocs.app

import kotlinx.browser.window

private const val KEY = "shell_atlas_session_active"

class WebSessionPreferences : SessionPreferences {
    override fun loadSessionFlag(): Boolean =
        window.localStorage.getItem(KEY) == "true"

    override fun saveSessionFlag(loggedIn: Boolean) {
        if (loggedIn) window.localStorage.setItem(KEY, "true")
        else window.localStorage.removeItem(KEY)
    }
}
