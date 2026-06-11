package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserRole

/**
 * Role assignments backed by the Supabase `user_roles` table.
 */
interface RoleRepository {

    suspend fun roleOf(userId: String): DomainResult<UserRole>

    suspend fun teamMembers(): DomainResult<List<TeamMember>>

    suspend fun assignRole(userId: String, role: UserRole): DomainResult<Unit>
}
