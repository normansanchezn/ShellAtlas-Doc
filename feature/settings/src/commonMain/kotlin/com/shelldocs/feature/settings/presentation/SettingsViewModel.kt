package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.error.toErrorDialogState
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
            SettingsIntent.SaveChanges -> saveChanges()
            SettingsIntent.SignOut -> signOut()
            SettingsIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun initialize() {
        setState { copy(loadingMessage = "Loading settings...", errorDialog = null) }
        getTeamMembers()
            .onSuccess { members ->
                setState {
                    copy(
                        loadingMessage = null,
                        role = roleProvider(),
                        members = members,
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        role = roleProvider(),
                        errorDialog = error.toErrorDialogState("load the settings"),
                    )
                }
            }
    }

    private suspend fun delegateRole(userId: String, newRole: UserRole) {
        setState { copy(loadingMessage = "Updating team access...", errorDialog = null, saveMessage = null) }
        assignRole(actorRole = currentState.role, targetUserId = userId, newRole = newRole)
            .onSuccess {
                val members = getTeamMembers().getOrDefault(currentState.members)
                setState { copy(loadingMessage = null, members = members) }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("update team access"),
                    )
                }
            }
    }

    private fun saveChanges() {
        setState { copy(loadingMessage = "Saving settings...", errorDialog = null, saveMessage = null) }
        setState { copy(loadingMessage = null, saveMessage = "Changes saved") }
    }

    private suspend fun signOut() {
        setState { copy(loadingMessage = "Signing out...", errorDialog = null) }
        signOut.invoke()
            .onSuccess {
                setState { copy(loadingMessage = null) }
                sendEffect(SettingsEffect.SignedOut)
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("sign you out"),
                    )
                }
            }
    }
}
