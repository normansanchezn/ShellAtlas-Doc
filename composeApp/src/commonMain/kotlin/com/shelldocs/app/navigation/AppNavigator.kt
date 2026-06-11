package com.shelldocs.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal state-based navigator. Top-level destinations replace each other
 * (no deep stack is needed for the workspace shell), and the previous route
 * is kept so back gestures behave naturally on Android.
 */
class AppNavigator(initial: AppRoute = AppRoute.ASSISTANT) {

    private val mutableRoute = MutableStateFlow(initial)
    val route: StateFlow<AppRoute> = mutableRoute.asStateFlow()

    private var previous: AppRoute? = null

    fun navigate(destination: AppRoute) {
        if (destination == mutableRoute.value) return
        previous = mutableRoute.value
        mutableRoute.value = destination
    }

    /** @return true when the back press was consumed. */
    fun navigateBack(): Boolean {
        val target = previous ?: return false
        previous = null
        mutableRoute.value = target
        return true
    }
}
