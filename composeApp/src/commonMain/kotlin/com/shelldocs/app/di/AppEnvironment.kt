package com.shelldocs.app.di

private const val AppEnvironmentKey = "SHELLDOC_APP_ENVIRONMENT"
private const val LegacyProfileKey = "SHELLDOC_PROFILE"

enum class AppEnvironment {
    DEV,
    PROD,
}

private val placeholderMarkers = listOf(
    "your-",
    "example.com",
    "your-project.supabase.co",
)

fun parseAppEnvironment(rawValue: String?): AppEnvironment =
    when (rawValue?.trim()?.uppercase()) {
        "PROD", "PRODUCTION" -> AppEnvironment.PROD
        else -> AppEnvironment.DEV
    }

internal fun resolveAppEnvironment(readSetting: (String) -> String?): AppEnvironment =
    parseAppEnvironment(resolveSetting(readSetting, AppEnvironmentKey, LegacyProfileKey))

internal fun resolveProfileSetting(
    readSetting: (String) -> String?,
    environment: AppEnvironment,
    key: String,
): String? {
    val profilePrefix = when (environment) {
        AppEnvironment.DEV -> "SHELLDOC_DEV_"
        AppEnvironment.PROD -> "SHELLDOC_PROD_"
    }

    return resolveSetting(
        readSetting,
        "$profilePrefix$key",
        "SHELLDOC_$key",
        key,
    )
}

internal fun resolveSetting(
    readSetting: (String) -> String?,
    vararg keys: String,
): String? =
    keys.asSequence()
        .mapNotNull { normalizeRuntimeSetting(readSetting(it)) }
        .firstOrNull()

internal fun normalizeRuntimeSetting(value: String?): String? =
    value
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.takeUnless(::isPlaceholderSetting)

internal fun normalizeSupabaseAnonKey(value: String?): String? =
    normalizeRuntimeSetting(value)
        ?.takeUnless(::isSecretSupabaseKey)

internal fun isPlaceholderSetting(value: String): Boolean {
    val normalized = value.trim().lowercase()
    return placeholderMarkers.any(normalized::contains)
}

internal fun isSecretSupabaseKey(value: String): Boolean =
    value.trim().lowercase().startsWith("sb_secret_")

internal fun resolveBooleanSetting(
    readSetting: (String) -> String?,
    environment: AppEnvironment,
    key: String,
    defaultValue: Boolean = false,
): Boolean =
    resolveProfileSetting(readSetting, environment, key)
        ?.equals("true", ignoreCase = true)
        ?: defaultValue
