package com.shelldocs.app

import android.content.Context

private const val PREFS_NAME = "shell_atlas_prefs"
private const val KEY = "dark_theme"

class AndroidThemePreferences(context: Context) : ThemePreferences {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): Boolean? =
        if (prefs.contains(KEY)) prefs.getBoolean(KEY, false) else null

    override fun save(isDark: Boolean?) {
        if (isDark == null) prefs.edit().remove(KEY).apply()
        else prefs.edit().putBoolean(KEY, isDark).apply()
    }
}
