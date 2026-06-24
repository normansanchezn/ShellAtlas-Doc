package com.shelldocs.app

import com.shelldocs.core.domain.entity.auth.AppLanguage
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
private data class PersistedAuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val userId: String,
    val email: String,
    val fullName: String,
    val team: String,
    val role: String,
    val language: String,
)

private val sessionJson = Json { ignoreUnknownKeys = true }

/** Serializes the full token + profile so [decodePersistedAuthSession] can restore it on relaunch. */
@OptIn(ExperimentalTime::class)
internal fun AuthSession.toPersistedJson(): String = sessionJson.encodeToString(
    PersistedAuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAtEpochSeconds = expiresAt.epochSeconds,
        userId = user.id,
        email = user.email,
        fullName = user.fullName,
        team = user.team,
        role = user.role.key,
        language = user.language.code,
    ),
)

/** Returns null on malformed/corrupted storage rather than crashing app startup. */
@OptIn(ExperimentalTime::class)
internal fun decodePersistedAuthSession(raw: String): AuthSession? = runCatching {
    val persisted = sessionJson.decodeFromString<PersistedAuthSession>(raw)
    AuthSession(
        accessToken = persisted.accessToken,
        refreshToken = persisted.refreshToken,
        expiresAt = Instant.fromEpochSeconds(persisted.expiresAtEpochSeconds),
        user = UserProfile(
            id = persisted.userId,
            email = persisted.email,
            fullName = persisted.fullName,
            team = persisted.team,
            role = UserRole.fromKey(persisted.role),
            language = AppLanguage.fromCode(persisted.language),
        ),
    )
}.getOrNull()
