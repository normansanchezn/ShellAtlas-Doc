package com.shelldocs.app

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.auth.AppLanguage
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ExperimentalTime

/**
 * Wraps any [AuthRepository] and persists the full token (not just a login flag) via
 * [SessionPreferences], so a relaunch restores the session as long as [AuthSession.expiresAt]
 * hasn't passed yet — the user only has to sign in again once the token actually expires.
 */
internal class AppAuthRepository(
    private val delegate: AuthRepository,
    private val sessionPrefs: SessionPreferences,
    private val timeProvider: TimeProvider,
) : AuthRepository {

    override val session: StateFlow<AuthSession?> = delegate.session

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> =
        delegate.signIn(credentials).onSuccess { session ->
            sessionPrefs.saveSessionFlag(true)
            sessionPrefs.saveAuthSessionToken(session.toPersistedJson())
        }

    override suspend fun signOut(): DomainResult<Unit> =
        delegate.signOut().also {
            sessionPrefs.saveSessionFlag(false)
            sessionPrefs.saveAssistantConversationId(null)
            sessionPrefs.saveAuthSessionToken(null)
        }

    @OptIn(ExperimentalTime::class)
    override suspend fun restoreSession(): DomainResult<AuthSession?> {
        delegate.restoreSession().onSuccess { if (it != null) return DomainResult.success(it) }

        val persisted = sessionPrefs.loadAuthSessionToken()?.let(::decodePersistedAuthSession)
        if (persisted == null || persisted.expiresAt <= timeProvider.now()) {
            sessionPrefs.saveAuthSessionToken(null)
            return DomainResult.success(null)
        }
        delegate.adoptSession(persisted)
        return DomainResult.success(persisted)
    }

    override fun adoptSession(session: AuthSession) {
        delegate.adoptSession(session)
    }

    override suspend fun updateLanguage(language: AppLanguage): DomainResult<UserProfile> =
        delegate.updateLanguage(language)
}
