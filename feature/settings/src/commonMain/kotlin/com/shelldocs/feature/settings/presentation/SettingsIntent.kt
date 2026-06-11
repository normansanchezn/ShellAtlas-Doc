package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.mvi.MviIntent
import com.shelldocs.core.domain.entity.auth.UserRole

sealed interface SettingsIntent : MviIntent {
    data object Initialize : SettingsIntent
    data class SelectSection(val section: SettingsSection) : SettingsIntent
    data class AssignRole(val userId: String, val role: UserRole) : SettingsIntent
    data class SetTwoFactorRequired(val enabled: Boolean) : SettingsIntent
    data class SetAuditLogEnabled(val enabled: Boolean) : SettingsIntent
    data class SetNotifyOutdatedDocs(val enabled: Boolean) : SettingsIntent
    data class SetNotifySyncFailures(val enabled: Boolean) : SettingsIntent
    data class SetNotifyWeeklyDigest(val enabled: Boolean) : SettingsIntent
    data object SaveChanges : SettingsIntent
    data object SignOut : SettingsIntent
    data object DismissError : SettingsIntent
}
