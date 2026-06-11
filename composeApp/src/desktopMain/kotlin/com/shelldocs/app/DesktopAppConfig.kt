package com.shelldocs.app

import com.shelldocs.app.di.AppConfig
import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig
import java.io.File

fun loadDesktopAppConfig(): AppConfig {
    val fileEnv = loadDotEnv()
    fun setting(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() } ?: fileEnv[name]

    val supabaseUrl = setting("SHELLDOC_SUPABASE_URL")
    val supabaseAnonKey = setting("SHELLDOC_SUPABASE_ANON_KEY")
    val apiBaseUrl = setting("SHELLDOC_API_BASE_URL")
    val apiBearerToken = setting("SHELLDOC_API_BEARER_TOKEN")
    val useOllama = setting("SHELLDOC_USE_OLLAMA")?.equals("true", ignoreCase = true) == true
    val ollamaBaseUrl = setting("SHELLDOC_OLLAMA_BASE_URL") ?: "http://127.0.0.1:11434"
    val ollamaModel = setting("SHELLDOC_OLLAMA_MODEL") ?: "llama3.1"

    return AppConfig(
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
}

private fun loadDotEnv(): Map<String, String> {
    val workingDirectory = File(System.getProperty("user.dir"))
    val envFile = sequenceOf(
        workingDirectory.resolve(".env"),
        workingDirectory.parentFile?.resolve(".env"),
    ).filterNotNull().firstOrNull(File::exists) ?: return emptyMap()

    return envFile.readLines()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val separator = line.indexOf('=')
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim().removeSurrounding("\"")
            key to value
        }
}
