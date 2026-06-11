package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import com.shelldocs.core.data.supabase.SupabaseProfileDataSource
import com.shelldocs.core.data.supabase.dto.UserRoleRowDto
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.RoleRepository

/**
 * Role delegation backed by the `user_roles` table; RLS allows members to
 * read all assignments but only owners to write them.
 */
class SupabaseRoleRepository(
    private val postgrest: SupabasePostgrestApi,
    private val profiles: SupabaseProfileDataSource,
    private val currentUserIdProvider: () -> String?,
) : RoleRepository {

    override suspend fun roleOf(userId: String): DomainResult<UserRole> = runCatching {
        val rows = postgrest.select<List<UserRoleRowDto>>(
            table = "user_roles",
            query = "user_id=eq.$userId&select=user_id,role_key",
        )
        UserRole.fromKey(rows.firstOrNull()?.roleKey)
    }.fold(
        onSuccess = { DomainResult.success(it) },
        onFailure = { DomainResult.failure(AppError.Network(it.message ?: "Role lookup failed")) },
    )

    override suspend fun teamMembers(): DomainResult<List<TeamMember>> = runCatching {
        val roleRows = postgrest.select<List<UserRoleRowDto>>(
            table = "user_roles",
            query = "select=user_id,role_key",
        )
        val rolesByUser = roleRows.associate { it.userId to UserRole.fromKey(it.roleKey) }
        val currentUserId = currentUserIdProvider()
        profiles.allProfiles().map { profile ->
            TeamMember(
                profile = UserProfile(
                    id = profile.id,
                    email = profile.email,
                    fullName = profile.fullName,
                    team = profile.team,
                    role = rolesByUser[profile.id] ?: UserRole.VIEWER,
                ),
                isCurrentUser = profile.id == currentUserId,
            )
        }
    }.fold(
        onSuccess = { DomainResult.success(it) },
        onFailure = { DomainResult.failure(AppError.Network(it.message ?: "Members lookup failed")) },
    )

    override suspend fun assignRole(userId: String, role: UserRole): DomainResult<Unit> = runCatching {
        postgrest.upsert(
            table = "user_roles",
            body = listOf(UserRoleRowDto(userId = userId, roleKey = role.key)),
        )
    }.fold(
        onSuccess = { DomainResult.success(Unit) },
        onFailure = { DomainResult.failure(AppError.Unauthorized(it.message ?: "Role assignment rejected")) },
    )
}
