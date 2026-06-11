package com.shelldocs.core.domain.entity.auth

/**
 * Single source of truth for the role -> capability matrix.
 *
 * - OWNER: full control of content, members and integrations.
 * - DEVELOP: creates and maintains technical documentation.
 * - BUSINESS: consumes knowledge and analytics, never mutates content.
 * - VIEWER: read-only fallback for unassigned users.
 */
object RolePermissions {

    private val matrix: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.OWNER to Permission.entries.toSet(),
        UserRole.DEVELOP to setOf(
            Permission.VIEW_DOCUMENTS,
            Permission.EDIT_DOCUMENTS,
            Permission.PUBLISH_DOCUMENTS,
            Permission.USE_ASSISTANT,
            Permission.VIEW_ANALYTICS,
            Permission.RUN_SOURCE_SYNC,
        ),
        UserRole.BUSINESS to setOf(
            Permission.VIEW_DOCUMENTS,
            Permission.USE_ASSISTANT,
            Permission.VIEW_ANALYTICS,
        ),
        UserRole.VIEWER to setOf(
            Permission.VIEW_DOCUMENTS,
        ),
    )

    fun permissionsFor(role: UserRole): Set<Permission> = matrix.getValue(role)

    fun isGranted(role: UserRole, permission: Permission): Boolean =
        permission in permissionsFor(role)
}
