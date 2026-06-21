package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.designsystem.i18n.stringsFor
import com.shelldocs.core.domain.entity.auth.AppLanguage
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.usecase.auth.AssignRoleUseCase
import com.shelldocs.core.domain.usecase.auth.GetTeamMembersUseCase
import com.shelldocs.core.domain.usecase.auth.SignOutUseCase
import com.shelldocs.core.domain.usecase.auth.UpdateLanguageUseCase
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val getTeamMembers: GetTeamMembersUseCase,
    private val assignRole: AssignRoleUseCase,
    private val signOut: SignOutUseCase,
    private val updateLanguage: UpdateLanguageUseCase,
    private val roleProvider: () -> UserRole,
    private val languageProvider: () -> AppLanguage,
    dispatchers: DispatcherProvider,
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>(SettingsState(), dispatchers) {

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Initialize -> initialize()
            is SettingsIntent.SelectSection ->
                setState { copy(selectedSection = intent.section, saveMessage = null) }
            is SettingsIntent.SetLanguage -> changeLanguage(intent.language)
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
        val strings = stringsFor(languageProvider())
        setState { copy(loadingMessage = strings.settingsLoadingSettings, errorDialog = null) }
        withContext(dispatchers.io) {
            getTeamMembers()
        }
            .onSuccess { members ->
                setState {
                    copy(
                        loadingMessage = null,
                        role = roleProvider(),
                        language = languageProvider(),
                        members = members,
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        role = roleProvider(),
                        language = languageProvider(),
                        errorDialog = error.toErrorDialogState("load the settings"),
                    )
                }
            }
    }

    private suspend fun changeLanguage(language: AppLanguage) {
        withContext(dispatchers.io) {
            updateLanguage(language)
        }
            .onSuccess {
                setState { copy(language = language) }
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("change the language")) }
            }
    }

    private suspend fun delegateRole(userId: String, newRole: UserRole) {
        val strings = stringsFor(currentState.language)
        setState { copy(loadingMessage = strings.settingsUpdatingTeamAccess, errorDialog = null, saveMessage = null) }
        withContext(dispatchers.io) {
            assignRole(actorRole = currentState.role, targetUserId = userId, newRole = newRole)
        }
            .onSuccess {
                val members = withContext(dispatchers.io) {
                    getTeamMembers().getOrDefault(currentState.members)
                }
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
        val strings = stringsFor(currentState.language)
        setState { copy(loadingMessage = strings.settingsSavingSettings, errorDialog = null, saveMessage = null) }
        setState { copy(loadingMessage = null, saveMessage = strings.settingsChangesSaved) }
    }

    private suspend fun signOut() {
        val strings = stringsFor(currentState.language)
        setState { copy(loadingMessage = strings.settingsSigningOut, errorDialog = null) }
        withContext(dispatchers.io) {
            signOut.invoke()
        }
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
