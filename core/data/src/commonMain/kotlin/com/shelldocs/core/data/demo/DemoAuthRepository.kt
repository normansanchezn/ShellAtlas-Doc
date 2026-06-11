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

/**
 * Demo identity provider: any syntactically valid credential signs in as the
 * workspace owner (Elena Vargas). Used when no Supabase project is configured.
 */
class DemoAuthRepository(private val timeProvider: TimeProvider) : AuthRepository {

    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession.asStateFlow()

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> {
        println("[ShellDocsAuth] Demo sign-in for ${credentials.email}")
        val authSession = AuthSession(
            accessToken = "demo-access-token",
            refreshToken = "demo-refresh-token",
            expiresAt = timeProvider.now() + 1.hours,
            user = DemoSeed.elena,
        )
        mutableSession.value = authSession
        println("[ShellDocsAuth] Demo sign-in success as ${authSession.user.email}")
        return DomainResult.success(authSession)
    }

    override suspend fun signOut(): DomainResult<Unit> {
        mutableSession.value = null
        return DomainResult.success(Unit)
    }

    override suspend fun restoreSession(): DomainResult<AuthSession?> =
        DomainResult.success(mutableSession.value)
}
