package com.shelldocs.core.data.demo

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.RoleRepository

/** In-memory role table mirroring the Supabase `user_roles` semantics. */
class DemoRoleRepository : RoleRepository {

    private val members = DemoSeed.teamMembers
        .associateBy { it.profile.id }
        .toMutableMap()

    override suspend fun roleOf(userId: String): DomainResult<UserRole> =
        members[userId]?.let { DomainResult.success(it.profile.role) }
            ?: DomainResult.failure(AppError.NotFound("Unknown member"))

    override suspend fun teamMembers(): DomainResult<List<TeamMember>> =
        DomainResult.success(members.values.sortedBy { it.profile.fullName })

    override suspend fun assignRole(userId: String, role: UserRole): DomainResult<Unit> {
        val member = members[userId]
            ?: return DomainResult.failure(AppError.NotFound("Unknown member"))
        members[userId] = member.copy(profile = member.profile.copy(role = role))
        return DomainResult.success(Unit)
    }
}
