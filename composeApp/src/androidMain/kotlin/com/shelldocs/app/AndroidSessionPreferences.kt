package com.shelldocs.app

import android.content.Context

private const val PREFS_NAME = "shell_atlas_prefs"
private const val KEY = "session_active"
private const val ASSISTANT_CONVERSATION_KEY = "assistant_active_conversation_id"

class AndroidSessionPreferences(context: Context) : SessionPreferences {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadSessionFlag(): Boolean = prefs.getBoolean(KEY, false)

    override fun saveSessionFlag(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY, loggedIn).apply()
    }

    override fun loadAssistantConversationId(): String? =
        prefs.getString(ASSISTANT_CONVERSATION_KEY, null)

    override fun saveAssistantConversationId(conversationId: String?) {
        prefs.edit().apply {
            if (conversationId == null) {
                remove(ASSISTANT_CONVERSATION_KEY)
            } else {
                putString(ASSISTANT_CONVERSATION_KEY, conversationId)
            }
        }.apply()
    }
}
