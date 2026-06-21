package com.shelldocs.app

import com.shelldocs.app.di.*
import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import com.shelldocs.core.data.assistant.OllamaConfig
import com.shelldocs.core.data.network.ApiConfig
import com.shelldocs.core.data.supabase.SupabaseConfig
import java.io.File

private val logger = AppLogger.tag(LogTags.STARTUP)

fun loadDesktopAppConfig(): AppConfig {
    val flavor = System.getProperty("shelldocs.flavor")
        ?: System.getenv("SHELLDOC_FLAVOR")
        ?: "demo"
    if (flavor.lowercase() == "demo") {
        logger.i("Desktop config: DEMO mode (in-memory data, local Ollama)")
        return AppConfig()
    }

    val fileEnv = loadDotEnv(flavor)
    fun setting(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() } ?: fileEnv[name]

    val environment = resolveAppEnvironment(::setting)
    // Falls back to values baked into the binary at build time (see
    // generateDesktopBuildConfig in build.gradle.kts) so a packaged .dmg
    // shared to another machine — with no env vars or .env file of its own —
    // still launches configured, instead of silently degrading to demo mode.
    val supabaseUrl = resolveProfileSetting(::setting, environment, "SUPABASE_URL")
        ?: DesktopBuildConfig.SUPABASE_URL.ifBlank { null }
    val supabaseAnonKey = normalizeSupabaseAnonKey(
        resolveProfileSetting(::setting, environment, "SUPABASE_ANON_KEY")
            ?: DesktopBuildConfig.SUPABASE_ANON_KEY.ifBlank { null },
    )
    val apiBaseUrl = resolveProfileSetting(::setting, environment, "API_BASE_URL")
        ?: DesktopBuildConfig.API_BASE_URL.ifBlank { null }
    val apiBearerToken = resolveProfileSetting(::setting, environment, "API_BEARER_TOKEN")
        ?: DesktopBuildConfig.API_BEARER_TOKEN.ifBlank { null }
    val useOllama = resolveBooleanSetting(::setting, environment, "USE_OLLAMA")
            || DesktopBuildConfig.USE_OLLAMA
    val ollamaBaseUrl = resolveProfileSetting(::setting, environment, "OLLAMA_BASE_URL")
        ?: DesktopBuildConfig.OLLAMA_BASE_URL.ifBlank { "http://127.0.0.1:11434" }
    val ollamaModel = resolveProfileSetting(::setting, environment, "OLLAMA_MODEL")
        ?: DesktopBuildConfig.OLLAMA_MODEL.ifBlank { "llama3.2" }

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

    logger.i(
        "Desktop config loaded. env=${config.environment}, " +
                "demoMode=${config.isDemoMode}, supabaseUrl=${config.supabase?.url ?: "none"}, " +
                "ollamaUrl=${config.ollama.baseUrl}, useOllama=${config.useOllama}, " +
                "apiBaseUrl=${config.api?.baseUrl ?: "none"}",
    )

    return config
}

private fun loadDotEnv(flavor: String = "demo"): Map<String, String> {
    val workingDirectory = File(System.getProperty("user.dir"))
    val envFile = sequenceOf(
        workingDirectory.resolve(".env.$flavor"),
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
