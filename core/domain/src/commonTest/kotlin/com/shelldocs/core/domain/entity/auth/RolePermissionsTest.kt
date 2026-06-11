package com.shelldocs.core.domain.entity.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RolePermissionsTest {

    @Test
    fun ownerHasEveryPermission() {
        assertEquals(Permission.entries.toSet(), RolePermissions.permissionsFor(UserRole.OWNER))
    }

    @Test
    fun developCanEditButNotManageMembers() {
        assertTrue(RolePermissions.isGranted(UserRole.DEVELOP, Permission.EDIT_DOCUMENTS))
        assertTrue(RolePermissions.isGranted(UserRole.DEVELOP, Permission.PUBLISH_DOCUMENTS))
        assertFalse(RolePermissions.isGranted(UserRole.DEVELOP, Permission.MANAGE_MEMBERS))
        assertFalse(RolePermissions.isGranted(UserRole.DEVELOP, Permission.DELETE_DOCUMENTS))
    }

    @Test
    fun businessIsReadAndAnalyticsOnly() {
        assertTrue(RolePermissions.isGranted(UserRole.BUSINESS, Permission.VIEW_ANALYTICS))
        assertTrue(RolePermissions.isGranted(UserRole.BUSINESS, Permission.USE_ASSISTANT))
        assertFalse(RolePermissions.isGranted(UserRole.BUSINESS, Permission.EDIT_DOCUMENTS))
        assertFalse(RolePermissions.isGranted(UserRole.BUSINESS, Permission.RUN_SOURCE_SYNC))
    }

    @Test
    fun viewerOnlyViewsDocuments() {
        assertEquals(setOf(Permission.VIEW_DOCUMENTS), RolePermissions.permissionsFor(UserRole.VIEWER))
    }

    @Test
    fun everyRoleHasAnExplicitMatrixEntry() {
        UserRole.entries.forEach { role ->
            assertTrue(RolePermissions.permissionsFor(role).isNotEmpty(), "Role $role has no permissions")
        }
    }
}
