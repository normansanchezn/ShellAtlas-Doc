package com.shelldocs.app

import platform.Foundation.NSUserDefaults

private const val KEY = "shell_atlas_session_active"

class IosSessionPreferences : SessionPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun loadSessionFlag(): Boolean = defaults.boolForKey(KEY)

    override fun saveSessionFlag(loggedIn: Boolean) {
        defaults.setBool(loggedIn, KEY)
    }
}
