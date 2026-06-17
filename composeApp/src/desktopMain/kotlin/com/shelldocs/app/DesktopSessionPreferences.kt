package com.shelldocs.app

import java.util.prefs.Preferences

private const val KEY = "session_active"

class DesktopSessionPreferences : SessionPreferences {
    private val node: Preferences = Preferences.userRoot().node("com/shelldocs/app")

    override fun loadSessionFlag(): Boolean = node.getBoolean(KEY, false)

    override fun saveSessionFlag(loggedIn: Boolean) {
        node.putBoolean(KEY, loggedIn)
        node.flush()
    }
}
