package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.supabase.SupabaseAuthApi
import com.shelldocs.core.data.supabase.SupabaseAuthException
import com.shelldocs.core.data.supabase.SupabaseProfileDataSource
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.repository.AuthRepository
import com.shelldocs.core.domain.repository.RoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

/**
 * Production [AuthRepository]: GoTrue password sign-in enriched with the
 * profile row and the delegated role from `user_roles`.
 */
class SupabaseAuthRepository(
    private val authApi: SupabaseAuthApi,
    private val profiles: SupabaseProfileDataSource,
    private val roleRepository: RoleRepository,
    private val timeProvider: TimeProvider,
) : AuthRepository {

    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession.asStateFlow()

    /** Exposed so the PostgREST client can attach the user's bearer token. */
    val accessToken: String? get() = mutableSession.value?.accessToken

    val currentUserId: String? get() = mutableSession.value?.user?.id

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> = try {
        val token = authApi.signInWithPassword(credentials.email, credentials.password)
        mutableSession.value = bootstrapSession(token.accessToken, token.refreshToken, token.expiresInSeconds, token.user.id, token.user.email)
        val profile = profiles.profile(token.user.id)
        val role = roleRepository.roleOf(token.user.id).getOrDefault(com.shelldocs.core.domain.entity.auth.UserRole.VIEWER)
        val authSession = AuthSession(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            expiresAt = timeProvider.now() + token.expiresInSeconds.seconds,
            user = UserProfile(
                id = token.user.id,
                email = token.user.email.ifBlank { credentials.email },
                fullName = profile?.fullName ?: credentials.email.substringBefore('@'),
                team = profile?.team ?: "",
                role = role,
            ),
        )
        mutableSession.value = authSession
        DomainResult.success(authSession)
    } catch (exception: SupabaseAuthException) {
        mutableSession.value = null
        DomainResult.failure(AppError.Unauthorized(exception.message ?: "Invalid credentials"))
    } catch (exception: Exception) {
        mutableSession.value = null
        DomainResult.failure(AppError.Network(exception.message ?: "Could not reach Supabase"))
    }

    override suspend fun signOut(): DomainResult<Unit> {
        val token = mutableSession.value?.accessToken
        mutableSession.value = null
        return try {
            token?.let { authApi.signOut(it) }
            DomainResult.success(Unit)
        } catch (_: Exception) {
            DomainResult.success(Unit) // local sign-out always succeeds
        }
    }

    override suspend fun restoreSession(): DomainResult<AuthSession?> =
        DomainResult.success(mutableSession.value)

    private fun bootstrapSession(
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long,
        userId: String,
        email: String,
    ): AuthSession = AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = timeProvider.now() + expiresInSeconds.seconds,
        user = UserProfile(
            id = userId,
            email = email,
            fullName = email.substringBefore('@'),
            team = "",
            role = com.shelldocs.core.domain.entity.auth.UserRole.VIEWER,
        ),
    )
}
