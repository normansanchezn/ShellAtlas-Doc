package com.shelldocs.core.common.persistence

/**
 * Persists session-scoped UI state for the app so authenticated users can
 * resume the same workspace after navigation changes or relaunches.
 */
interface SessionPreferences {
    fun loadSessionFlag(): Boolean
    fun saveSessionFlag(loggedIn: Boolean)
    fun loadAssistantConversationId(): String?
    fun saveAssistantConversationId(conversationId: String?)

    /**
     * Raw serialized auth session (tokens + expiry + profile), opaque to this layer.
     * Lets the app skip the login screen after a relaunch as long as the token hasn't expired.
     */
    fun loadAuthSessionToken(): String?
    fun saveAuthSessionToken(token: String?)
}

object NoOpSessionPreferences : SessionPreferences {
    override fun loadSessionFlag() = false
    override fun saveSessionFlag(loggedIn: Boolean) {}
    override fun loadAssistantConversationId(): String? = null
    override fun saveAssistantConversationId(conversationId: String?) {}
    override fun loadAuthSessionToken(): String? = null
    override fun saveAuthSessionToken(token: String?) {}
}
