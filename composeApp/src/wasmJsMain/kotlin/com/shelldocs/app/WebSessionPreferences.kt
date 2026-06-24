package com.shelldocs.app

import kotlinx.browser.window

private const val KEY = "shell_atlas_session_active"
private const val ASSISTANT_CONVERSATION_KEY = "shell_atlas_assistant_active_conversation_id"
private const val AUTH_SESSION_TOKEN_KEY = "shell_atlas_auth_session_token"

class WebSessionPreferences : SessionPreferences {
    override fun loadSessionFlag(): Boolean =
        window.localStorage.getItem(KEY) == "true"

    override fun saveSessionFlag(loggedIn: Boolean) {
        if (loggedIn) window.localStorage.setItem(KEY, "true")
        else window.localStorage.removeItem(KEY)
    }

    override fun loadAssistantConversationId(): String? =
        window.localStorage.getItem(ASSISTANT_CONVERSATION_KEY)

    override fun saveAssistantConversationId(conversationId: String?) {
        if (conversationId == null) {
            window.localStorage.removeItem(ASSISTANT_CONVERSATION_KEY)
        } else {
            window.localStorage.setItem(ASSISTANT_CONVERSATION_KEY, conversationId)
        }
    }

    override fun loadAuthSessionToken(): String? =
        window.localStorage.getItem(AUTH_SESSION_TOKEN_KEY)

    override fun saveAuthSessionToken(token: String?) {
        if (token == null) {
            window.localStorage.removeItem(AUTH_SESSION_TOKEN_KEY)
        } else {
            window.localStorage.setItem(AUTH_SESSION_TOKEN_KEY, token)
        }
    }
}
