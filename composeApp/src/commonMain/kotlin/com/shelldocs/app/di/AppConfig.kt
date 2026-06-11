package com.shelldocs.app.di

import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig

/**
 * Runtime wiring switches. With [supabase] or [api] unset the app runs in
 * demo mode: every screen works against the seeded in-memory repositories,
 * exactly like the Mock* sources of the original ShellEnterpriseDoc.
 */
data class AppConfig(
    val supabase: SupabaseConfig? = null,
    val api: ApiConfig? = null,
    val ollama: OllamaConfig = OllamaConfig(),
    val useOllama: Boolean = false,
) {
    val isDemoMode: Boolean = supabase == null && api == null
}
