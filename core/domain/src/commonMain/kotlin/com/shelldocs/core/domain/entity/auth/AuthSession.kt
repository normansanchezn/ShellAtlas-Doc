package com.shelldocs.core.domain.entity.auth

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Active Supabase session for the signed-in user. */
data class AuthSession @OptIn(ExperimentalTime::class) constructor(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: kotlin.time.Instant,
    val user: UserProfile,
)
