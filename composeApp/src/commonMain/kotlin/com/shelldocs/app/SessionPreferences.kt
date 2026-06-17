package com.shelldocs.app

/**
 * Persists whether the user has an active session so the app can skip the
 * login screen on next launch.  Each platform provides a concrete
 * implementation backed by its native key-value store.
 *
 * [NoOpSessionPreferences] is used in previews and tests.
 */
interface SessionPreferences {
    fun loadSessionFlag(): Boolean
    fun saveSessionFlag(loggedIn: Boolean)
}

object NoOpSessionPreferences : SessionPreferences {
    override fun loadSessionFlag() = false
    override fun saveSessionFlag(loggedIn: Boolean) {}
}
