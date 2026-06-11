package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.mvi.MviIntent

/** Everything the user can do on the sign-in screen. */
sealed interface AuthIntent : MviIntent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data object Submit : AuthIntent
    data object DismissError : AuthIntent
}
