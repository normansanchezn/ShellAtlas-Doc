package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserRole

/** Snapshot of the Settings page. */
data class SettingsState(
    val selectedSection: SettingsSection = SettingsSection.GENERAL,
    val role: UserRole = UserRole.VIEWER,
    val members: List<TeamMember> = emptyList(),
    val twoFactorRequired: Boolean = false,
    val auditLogEnabled: Boolean = true,
    val notifyOutdatedDocs: Boolean = true,
    val notifySyncFailures: Boolean = true,
    val notifyWeeklyDigest: Boolean = false,
    val saveMessage: String? = null,
    val errorMessage: String? = null,
) : MviState {

    val canManageMembers: Boolean = RolePermissions.isGranted(role, Permission.MANAGE_MEMBERS)
}
