package com.shelldocs.app

import android.content.Context

private const val PREFS_NAME = "shell_atlas_prefs"
private const val KEY = "session_active"

class AndroidSessionPreferences(context: Context) : SessionPreferences {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadSessionFlag(): Boolean = prefs.getBoolean(KEY, false)

    override fun saveSessionFlag(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY, loggedIn).apply()
    }
}
