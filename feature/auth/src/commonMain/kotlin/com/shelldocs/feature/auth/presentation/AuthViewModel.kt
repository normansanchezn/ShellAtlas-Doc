package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.error.ErrorDialogState
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
                setState { copy(isLoading = false, errorDialog = error.toSignInErrorDialogState()) }
            }
    }

    private fun AppError.toSignInErrorDialogState(): ErrorDialogState = when (this) {
        is AppError.Network -> ErrorDialogState(
            title = "We couldn't sign you in",
            message = "The sign-in service is unavailable right now. If you're working locally, start Supabase or clear the Supabase env vars to use demo mode.",
        )
        is AppError.Unauthorized -> ErrorDialogState(
            title = "We couldn't sign you in",
            message = "Check your email and password and try again.",
        )
        else -> toErrorDialogState("sign you in")
    }
}
