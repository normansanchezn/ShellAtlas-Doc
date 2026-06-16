package com.shelldocs.app

import android.util.Log
import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.normalizeSupabaseAnonKey
import com.shelldocs.app.di.parseAppEnvironment
import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig

private const val AuthTag = "ShellDocsAuth"

fun loadAndroidAppConfig(): AppConfig {
    val supabaseUrl = normalizeAndroidLocalhost(BuildConfig.SUPABASE_URL)
    val supabaseAnonKey = normalizeSupabaseAnonKey(BuildConfig.SUPABASE_ANON_KEY).orEmpty()
    val apiBaseUrl = normalizeAndroidLocalhost(BuildConfig.API_BASE_URL)
    val ollamaBaseUrl = normalizeAndroidLocalhost(BuildConfig.OLLAMA_BASE_URL)
    val environment = parseAppEnvironment(BuildConfig.APP_ENVIRONMENT)

    val config = AppConfig(
        environment = environment,
        supabase = if (supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()) {
            SupabaseConfig(url = supabaseUrl, anonKey = supabaseAnonKey)
        } else {
            null
        },
        api = if (apiBaseUrl.isNotBlank()) {
            ApiConfig(
                baseUrl = apiBaseUrl,
                bearerToken = BuildConfig.API_BEARER_TOKEN.ifBlank { null },
            )
        } else {
            null
        },
        ollama = OllamaConfig(
            baseUrl = ollamaBaseUrl,
            model = BuildConfig.OLLAMA_MODEL,
        ),
        useOllama = BuildConfig.USE_OLLAMA,
    )

    Log.i(
        AuthTag,
        "Android config loaded. env=${config.environment}, demoMode=${config.isDemoMode}, " +
            "supabaseUrl=${config.supabase?.url ?: "none"}",
    )

    return config
}

private fun normalizeAndroidLocalhost(url: String): String {
    if (url.isBlank()) return url
    return url
        .replace("://127.0.0.1", "://10.0.2.2")
        .replace("://localhost", "://10.0.2.2")
}
