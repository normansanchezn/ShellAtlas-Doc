package com.shelldocs.app

import platform.Foundation.NSUserDefaults

private const val KEY = "shell_atlas_session_active"
private const val ASSISTANT_CONVERSATION_KEY = "shell_atlas_assistant_active_conversation_id"

class IosSessionPreferences : SessionPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun loadSessionFlag(): Boolean = defaults.boolForKey(KEY)

    override fun saveSessionFlag(loggedIn: Boolean) {
        defaults.setBool(loggedIn, KEY)
    }

    override fun loadAssistantConversationId(): String? =
        defaults.stringForKey(ASSISTANT_CONVERSATION_KEY)

    override fun saveAssistantConversationId(conversationId: String?) {
        if (conversationId == null) {
            defaults.removeObjectForKey(ASSISTANT_CONVERSATION_KEY)
        } else {
            defaults.setObject(conversationId, ASSISTANT_CONVERSATION_KEY)
        }
    }
}
