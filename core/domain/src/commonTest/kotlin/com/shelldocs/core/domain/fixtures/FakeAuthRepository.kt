@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.fixtures

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.*
import com.shelldocs.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

class FakeAuthRepository : AuthRepository {

    var lastCredentials: SignInCredentials? = null
    var nextResult: DomainResult<AuthSession> = DomainResult.success(sampleSession())

    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> {
        lastCredentials = credentials
        val result = nextResult
        if (result is DomainResult.Success) mutableSession.value = result.value
        return result
    }

    override suspend fun signOut(): DomainResult<Unit> {
        mutableSession.value = null
        return DomainResult.success(Unit)
    }

    override suspend fun restoreSession(): DomainResult<AuthSession?> =
        DomainResult.success(mutableSession.value)

    override suspend fun updateLanguage(language: AppLanguage): DomainResult<UserProfile> {
        val current = mutableSession.value ?: return DomainResult.failure(AppError.Unauthorized())
        val updatedUser = current.user.copy(language = language)
        mutableSession.value = current.copy(user = updatedUser)
        return DomainResult.success(updatedUser)
    }

    companion object {
        fun sampleSession(role: UserRole = UserRole.OWNER): AuthSession = AuthSession(
            accessToken = "token",
            refreshToken = "refresh",
            expiresAt = Instant.parse("2030-06-02T00:00:00Z"),
            user = UserProfile(
                id = "user-1",
                email = "elena.vargas@shell.com",
                fullName = "Elena Vargas",
                team = "iOS Shell App",
                role = role,
            ),
        )

        fun unauthorized(): DomainResult<AuthSession> =
            DomainResult.failure(AppError.Unauthorized())
    }
}
