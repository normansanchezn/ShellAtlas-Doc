package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
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
}
