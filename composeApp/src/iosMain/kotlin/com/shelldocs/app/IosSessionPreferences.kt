package com.shelldocs.app

import platform.Foundation.NSUserDefaults

private const val KEY = "shell_atlas_session_active"
private const val ASSISTANT_CONVERSATION_KEY = "shell_atlas_assistant_active_conversation_id"
private const val AUTH_SESSION_TOKEN_KEY = "shell_atlas_auth_session_token"

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

    override fun loadAuthSessionToken(): String? =
        defaults.stringForKey(AUTH_SESSION_TOKEN_KEY)

    override fun saveAuthSessionToken(token: String?) {
        if (token == null) {
            defaults.removeObjectForKey(AUTH_SESSION_TOKEN_KEY)
        } else {
            defaults.setObject(token, AUTH_SESSION_TOKEN_KEY)
        }
    }
}
