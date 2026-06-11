package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.usecase.auth.SignInUseCase

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
                setState { copy(email = intent.value, errorMessage = null) }
            is AuthIntent.PasswordChanged ->
                setState { copy(password = intent.value, errorMessage = null) }
            AuthIntent.DismissError ->
                setState { copy(errorMessage = null) }
            AuthIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        val snapshot = currentState
        if (!snapshot.canSubmit) return
        setState { copy(isLoading = true, errorMessage = null) }
        signIn(SignInCredentials(email = snapshot.email, password = snapshot.password))
            .onSuccess {
                setState { copy(isLoading = false) }
                sendEffect(AuthEffect.NavigateToWorkspace)
            }
            .onFailure { error ->
                setState { copy(isLoading = false, errorMessage = error.message) }
            }
    }
}
