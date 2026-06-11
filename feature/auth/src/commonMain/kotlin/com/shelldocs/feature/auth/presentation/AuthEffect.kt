package com.shelldocs.feature.auth.presentation

import com.shelldocs.core.common.mvi.MviEffect

/** One-shot signals emitted by [AuthViewModel]. */
sealed interface AuthEffect : MviEffect {
    data object NavigateToWorkspace : AuthEffect
}
