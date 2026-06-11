package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.supabase.SupabaseAuthApi
import com.shelldocs.core.data.supabase.SupabaseAuthException
import com.shelldocs.core.data.supabase.SupabasePostgrestException
import com.shelldocs.core.data.supabase.SupabaseProfileDataSource
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.repository.AuthRepository
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
    private val roleRepository: SupabaseRoleRepository,
    private val timeProvider: TimeProvider,
) : AuthRepository {

    private val mutableSession = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = mutableSession.asStateFlow()

    /** Exposed so the PostgREST client can attach the user's bearer token. */
    val accessToken: String? get() = mutableSession.value?.accessToken

    val currentUserId: String? get() = mutableSession.value?.user?.id

    override suspend fun signIn(credentials: SignInCredentials): DomainResult<AuthSession> = try {
        println("[ShellDocsAuth] Starting Supabase sign-in for ${credentials.email}")
        val token = authApi.signInWithPassword(credentials.email, credentials.password)
        println("[ShellDocsAuth] Password grant succeeded for user ${token.user.id}")
        val profile = runCatching {
            profiles.profile(token.user.id, token.accessToken)
        }.getOrElse { error ->
            if (error is SupabasePostgrestException) {
                println("[ShellDocsAuth] Profile fetch skipped: ${error.message}")
                null
            } else {
                throw error
            }
        }
        println("[ShellDocsAuth] Profile fetch completed for ${token.user.id}")
        val role = runCatching {
            roleRepository
                .roleOf(token.user.id, token.accessToken)
                .getOrDefault(com.shelldocs.core.domain.entity.auth.UserRole.VIEWER)
        }.getOrElse { error ->
            if (error is SupabasePostgrestException) {
                println("[ShellDocsAuth] Role fetch skipped: ${error.message}")
                com.shelldocs.core.domain.entity.auth.UserRole.VIEWER
            } else {
                throw error
            }
        }
        println("[ShellDocsAuth] Role fetch completed: ${role.key}")
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
        println("[ShellDocsAuth] Supabase sign-in success as ${authSession.user.email}")
        DomainResult.success(authSession)
    } catch (exception: SupabaseAuthException) {
        mutableSession.value = null
        println("[ShellDocsAuth] Supabase sign-in rejected: ${exception.message}")
        DomainResult.failure(AppError.Unauthorized(exception.message ?: "Invalid credentials"))
    } catch (exception: Exception) {
        mutableSession.value = null
        println("[ShellDocsAuth] Supabase sign-in failed: ${exception.message}")
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
}
