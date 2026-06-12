package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.usecase.auth.SignInUseCase
import kotlinx.coroutines.withContext

/**
 * MVI loop of the AUTH feature — the reference implementation of the
 * clean-architecture stack: UI -> intent -> use case -> repository.
 */
class AuthViewModel(
    private val signIn: SignInUseCase,
    dispatchers: DispatcherProvider,
) : MviViewModel<AuthIntent, AuthState, AuthEffect>(AuthState(), dispatchers) {

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged ->
                setState { copy(email = intent.value, errorDialog = null) }
            is AuthIntent.PasswordChanged ->
                setState { copy(password = intent.value, errorDialog = null) }
            AuthIntent.DismissError ->
                setState { copy(errorDialog = null) }
            AuthIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        val snapshot = currentState
        if (!snapshot.canSubmit) return
        setState { copy(isLoading = true, errorDialog = null) }
        withContext(dispatchers.io) {
            signIn(SignInCredentials(email = snapshot.email, password = snapshot.password))
        }
            .onSuccess {
                setState { copy(isLoading = false) }
                sendEffect(AuthEffect.NavigateToWorkspace)
            }
            .onFailure { error ->
                setState { copy(isLoading = false, errorDialog = error.toErrorDialogState("sign you in")) }
            }
    }
}
