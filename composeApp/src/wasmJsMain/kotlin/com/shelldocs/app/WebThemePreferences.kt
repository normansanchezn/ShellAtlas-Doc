package com.shelldocs.app

import kotlinx.browser.window

private const val KEY = "shell_atlas_dark_theme"

class WebThemePreferences : ThemePreferences {
    override fun load(): Boolean? =
        window.localStorage.getItem(KEY)?.toBooleanStrictOrNull()

    override fun save(isDark: Boolean?) {
        if (isDark == null) window.localStorage.removeItem(KEY)
        else window.localStorage.setItem(KEY, isDark.toString())
    }
}
