package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.mvi.MviState

/** Immutable snapshot of the sign-in screen. */
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : MviState {
    val canSubmit: Boolean = email.isNotBlank() && password.isNotBlank() && !isLoading
}
