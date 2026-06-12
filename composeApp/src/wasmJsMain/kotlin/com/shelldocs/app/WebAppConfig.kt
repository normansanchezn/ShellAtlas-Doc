package com.shelldocs.app

import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.resolveAppEnvironment
import com.shelldocs.app.di.resolveBooleanSetting
import com.shelldocs.app.di.resolveProfileSetting
import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig
import kotlinx.browser.window

fun loadWebAppConfig(): AppConfig {
    val params = parseQueryParams(window.location.search)
    fun setting(name: String): String? = params[name]?.takeIf { it.isNotBlank() }

    val environment = resolveAppEnvironment(::setting)
    val supabaseUrl = resolveProfileSetting(::setting, environment, "SUPABASE_URL")
    val supabaseAnonKey = resolveProfileSetting(::setting, environment, "SUPABASE_ANON_KEY")
    val apiBaseUrl = resolveProfileSetting(::setting, environment, "API_BASE_URL")
    val apiBearerToken = resolveProfileSetting(::setting, environment, "API_BEARER_TOKEN")
    val useOllama = resolveBooleanSetting(::setting, environment, "USE_OLLAMA")
    val ollamaBaseUrl = resolveProfileSetting(::setting, environment, "OLLAMA_BASE_URL")
        ?: "http://127.0.0.1:11434"
    val ollamaModel = resolveProfileSetting(::setting, environment, "OLLAMA_MODEL")
        ?: "llama3.1"

    val config = AppConfig(
        environment = environment,
        supabase = if (!supabaseUrl.isNullOrBlank() && !supabaseAnonKey.isNullOrBlank()) {
            SupabaseConfig(url = supabaseUrl, anonKey = supabaseAnonKey)
        } else {
            null
        },
        api = if (!apiBaseUrl.isNullOrBlank()) {
            ApiConfig(baseUrl = apiBaseUrl, bearerToken = apiBearerToken)
        } else {
            null
        },
        ollama = OllamaConfig(baseUrl = ollamaBaseUrl, model = ollamaModel),
        useOllama = useOllama,
    )

    println(
        "[ShellDocsAuth] Web config loaded. env=${config.environment}, demoMode=${config.isDemoMode}, " +
            "supabaseUrl=${config.supabase?.url ?: "none"}",
    )

    return config
}

private fun parseQueryParams(search: String): Map<String, String> {
    if (search.isBlank()) return emptyMap()
    return search
        .removePrefix("?")
        .split("&")
        .mapNotNull { pair ->
            val separator = pair.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val key = decodeComponent(pair.substring(0, separator))
            val value = decodeComponent(pair.substring(separator + 1))
            key to value
        }
        .toMap()
}

private fun decodeComponent(value: String): String =
    value.replace("+", " ")
