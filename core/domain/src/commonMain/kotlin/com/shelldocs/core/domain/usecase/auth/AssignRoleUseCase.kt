package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.RoleRepository

/**
 * Role delegation. Authorization is enforced here (domain) in addition to the
 * Supabase RLS policy, so a compromised client still cannot escalate roles.
 */
class AssignRoleUseCase(private val roleRepository: RoleRepository) {

    suspend operator fun invoke(
        actorRole: UserRole,
        targetUserId: String,
        newRole: UserRole,
    ): DomainResult<Unit> {
        if (!RolePermissions.isGranted(actorRole, Permission.MANAGE_MEMBERS)) {
            return DomainResult.failure(AppError.Unauthorized("Only owners can manage member roles"))
        }
        return roleRepository.assignRole(targetUserId, newRole)
    }
}
