package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Demo identity provider: any syntactically valid credential signs in as the
 * workspace owner (Elena Vargas). Used when no Supabase project is configured.
 *
 * Pass [initiallyLoggedIn] = true to skip the login screen on app restart
 * (session is restored without requiring credentials again).
 */
@OptIn(ExperimentalTime::class)
class DemoAuthRepository(
    private val timeProvider: TimeProvider,
    initiallyLoggedIn: Boolean = false,
) : AuthRepository {

    private val mutableSession = MutableStateFlow<AuthSession?>(
        if (initiallyLoggedIn) buildSession(timeProvider) else null,
    )
    override val session: StateFlow<AuthSession?> = mutableSession.asStateFlow()

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> {
        val authSession = buildSession(timeProvider)
        mutableSession.value = authSession
        println("[ShellDocsAuth] Demo sign-in as ${authSession.user.email}")
        return DomainResult.success(authSession)
    }

    override suspend fun signOut(): DomainResult<Unit> {
        mutableSession.value = null
        return DomainResult.success(Unit)
    }

    override suspend fun restoreSession(): DomainResult<AuthSession?> =
        DomainResult.success(mutableSession.value)

    companion object {
        @OptIn(ExperimentalTime::class)
        internal fun buildSession(timeProvider: TimeProvider) = AuthSession(
            accessToken = "demo-access-token",
            refreshToken = "demo-refresh-token",
            expiresAt = timeProvider.now() + 1.hours,
            user = DemoSeed.elena,
        )
    }
}
