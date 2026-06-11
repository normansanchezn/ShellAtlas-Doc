package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.usecase.auth.AssignRoleUseCase
import com.shelldocs.core.domain.usecase.auth.GetTeamMembersUseCase
import com.shelldocs.core.domain.usecase.auth.SignOutUseCase

class SettingsViewModel(
    private val getTeamMembers: GetTeamMembersUseCase,
    private val assignRole: AssignRoleUseCase,
    private val signOut: SignOutUseCase,
    private val roleProvider: () -> UserRole,
    dispatchers: DispatcherProvider,
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>(SettingsState(), dispatchers) {

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Initialize -> initialize()
            is SettingsIntent.SelectSection ->
                setState { copy(selectedSection = intent.section, saveMessage = null) }
            is SettingsIntent.AssignRole -> delegateRole(intent.userId, intent.role)
            is SettingsIntent.SetTwoFactorRequired ->
                setState { copy(twoFactorRequired = intent.enabled, saveMessage = null) }
            is SettingsIntent.SetAuditLogEnabled ->
                setState { copy(auditLogEnabled = intent.enabled, saveMessage = null) }
            is SettingsIntent.SetNotifyOutdatedDocs ->
                setState { copy(notifyOutdatedDocs = intent.enabled, saveMessage = null) }
            is SettingsIntent.SetNotifySyncFailures ->
                setState { copy(notifySyncFailures = intent.enabled, saveMessage = null) }
            is SettingsIntent.SetNotifyWeeklyDigest ->
                setState { copy(notifyWeeklyDigest = intent.enabled, saveMessage = null) }
            SettingsIntent.SaveChanges ->
                setState { copy(saveMessage = "Changes saved") }
            SettingsIntent.SignOut -> {
                signOut.invoke()
                sendEffect(SettingsEffect.SignedOut)
            }
        }
    }

    private suspend fun initialize() {
        val members = getTeamMembers().getOrDefault(emptyList())
        setState { copy(role = roleProvider(), members = members, errorMessage = null) }
    }

    private suspend fun delegateRole(userId: String, newRole: UserRole) {
        assignRole(actorRole = currentState.role, targetUserId = userId, newRole = newRole)
            .onSuccess {
                val members = getTeamMembers().getOrDefault(currentState.members)
                setState { copy(members = members, errorMessage = null) }
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }
}
