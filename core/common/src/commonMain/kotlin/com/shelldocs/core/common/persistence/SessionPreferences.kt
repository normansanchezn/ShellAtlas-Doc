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
}

object NoOpSessionPreferences : SessionPreferences {
    override fun loadSessionFlag() = false
    override fun saveSessionFlag(loggedIn: Boolean) {}
    override fun loadAssistantConversationId(): String? = null
    override fun saveAssistantConversationId(conversationId: String?) {}
}
