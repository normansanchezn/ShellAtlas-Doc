package com.shelldocs.core.data.supabase

/** Connection settings for the Supabase project. */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
) {
    val authBaseUrl: String = "${url.trimEnd('/')}/auth/v1"
    val restBaseUrl: String = "${url.trimEnd('/')}/rest/v1"
}
