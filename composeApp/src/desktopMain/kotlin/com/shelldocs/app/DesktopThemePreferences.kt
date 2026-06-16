package com.shelldocs.app

import java.util.prefs.Preferences

private const val KEY = "dark_theme"
private const val UNSET = "__unset__"

class DesktopThemePreferences : ThemePreferences {
    private val node: Preferences = Preferences.userRoot().node("com/shelldocs/app")

    override fun load(): Boolean? {
        val raw = node.get(KEY, UNSET)
        return if (raw == UNSET) null else raw.toBooleanStrictOrNull()
    }

    override fun save(isDark: Boolean?) {
        if (isDark == null) node.remove(KEY) else node.put(KEY, isDark.toString())
        node.flush()
    }
}
