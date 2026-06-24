package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.AppLanguage
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.entity.auth.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Boundary to the identity provider (Supabase GoTrue in production).
 * The domain layer never sees tokens being refreshed or persisted.
 */
interface AuthRepository {

    val session: StateFlow<AuthSession?>

    suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession>

    suspend fun signOut(): DomainResult<Unit>

    suspend fun restoreSession(): DomainResult<AuthSession?>

    /** Hydrates in-memory state from a session persisted by the caller (e.g. on app relaunch). */
    fun adoptSession(session: AuthSession)

    /** Persists the user's display-language preference (`profiles.language`). */
    suspend fun updateLanguage(language: AppLanguage): DomainResult<UserProfile>
}
