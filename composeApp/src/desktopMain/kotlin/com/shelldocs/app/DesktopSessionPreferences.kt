package com.shelldocs.app

import java.util.prefs.Preferences

private const val KEY = "session_active"
private const val ASSISTANT_CONVERSATION_KEY = "assistant_active_conversation_id"

class DesktopSessionPreferences : SessionPreferences {
    private val node: Preferences = Preferences.userRoot().node("com/shelldocs/app")

    override fun loadSessionFlag(): Boolean = node.getBoolean(KEY, false)

    override fun saveSessionFlag(loggedIn: Boolean) {
        node.putBoolean(KEY, loggedIn)
        node.flush()
    }

    override fun loadAssistantConversationId(): String? = node.get(ASSISTANT_CONVERSATION_KEY, null)

    override fun saveAssistantConversationId(conversationId: String?) {
        if (conversationId == null) {
            node.remove(ASSISTANT_CONVERSATION_KEY)
        } else {
            node.put(ASSISTANT_CONVERSATION_KEY, conversationId)
        }
        node.flush()
    }
}
