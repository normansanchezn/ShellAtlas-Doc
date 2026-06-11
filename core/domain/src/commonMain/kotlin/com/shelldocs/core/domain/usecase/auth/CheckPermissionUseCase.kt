package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole

class CheckPermissionUseCase {

    operator fun invoke(role: UserRole, permission: Permission): Boolean =
        RolePermissions.isGranted(role, permission)
}
