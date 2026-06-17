package com.shelldocs.app

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Wraps any [AuthRepository] and persists session state via [SessionPreferences]
 * so the app skips the login screen on next launch.
 */
internal class AppAuthRepository(
    private val delegate: AuthRepository,
    private val sessionPrefs: SessionPreferences,
) : AuthRepository {

    override val session: StateFlow<AuthSession?> = delegate.session

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> =
        delegate.signIn(credentials).onSuccess { sessionPrefs.saveSessionFlag(true) }

    override suspend fun signOut(): DomainResult<Unit> =
        delegate.signOut().also { sessionPrefs.saveSessionFlag(false) }

    override suspend fun restoreSession(): DomainResult<AuthSession?> =
        delegate.restoreSession()
}
