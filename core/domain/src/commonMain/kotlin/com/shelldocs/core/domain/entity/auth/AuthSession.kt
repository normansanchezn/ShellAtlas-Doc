package com.shelldocs.core.domain.entity.auth

import kotlinx.datetime.Instant

/** Active Supabase session for the signed-in user. */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val user: UserProfile,
)
