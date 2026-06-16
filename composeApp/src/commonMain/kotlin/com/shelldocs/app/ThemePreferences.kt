package com.shelldocs.app

/**
 * Persists the user's explicit dark/light mode choice.
 *
 * [load] returns null when the user has never made an explicit choice; callers
 * should fall back to the system preference ([isSystemInDarkTheme]).  Once the
 * user toggles the theme from Settings, [save] is called with the chosen value
 * so future sessions start with that choice rather than the system default.
 *
 * The default [NoOpThemePreferences] is used in preview / tests where no
 * real storage is available.
 */
interface ThemePreferences {
    fun load(): Boolean?
    fun save(isDark: Boolean?)
}

object NoOpThemePreferences : ThemePreferences {
    override fun load(): Boolean? = null
    override fun save(isDark: Boolean?) {}
}
