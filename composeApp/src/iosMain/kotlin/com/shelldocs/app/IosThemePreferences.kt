package com.shelldocs.app

import platform.Foundation.NSUserDefaults

private const val KEY = "shell_atlas_dark_theme"

class IosThemePreferences : ThemePreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun load(): Boolean? =
        if (defaults.objectForKey(KEY) != null) defaults.boolForKey(KEY) else null

    override fun save(isDark: Boolean?) {
        if (isDark == null) defaults.removeObjectForKey(KEY)
        else defaults.setBool(isDark, KEY)
    }
}
