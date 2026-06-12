package com.shelldocs.app

import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.resolveAppEnvironment
import com.shelldocs.app.di.resolveBooleanSetting
import com.shelldocs.app.di.resolveProfileSetting
import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

fun loadWebAppConfig(): AppConfig {
    val params = URLSearchParams(window.location.search)
    fun setting(name: String): String? = params.get(name)?.takeIf { it.isNotBlank() }

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

    console.log(
        "[ShellDocsAuth] Web config loaded. env=${config.environment}, demoMode=${config.isDemoMode}, " +
            "supabaseUrl=${config.supabase?.url ?: "none"}",
    )

    return config
}
