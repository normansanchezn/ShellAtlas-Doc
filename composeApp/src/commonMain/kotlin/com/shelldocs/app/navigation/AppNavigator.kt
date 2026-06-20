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

    private val mutableOpenDocumentRequests = MutableStateFlow<String?>(null)

    /** Document id requested for opening in the Documents screen, if any. */
    val openDocumentRequests: StateFlow<String?> = mutableOpenDocumentRequests.asStateFlow()

    private val mutableAiUpdateRequests = MutableStateFlow<String?>(null)

    /** Document id requested for the AI Suggested Update screen, if any. */
    val aiUpdateRequests: StateFlow<String?> = mutableAiUpdateRequests.asStateFlow()

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

    /** Navigates to Documents and asks it to open and select [documentId]. */
    fun openDocument(documentId: String) {
        mutableOpenDocumentRequests.value = documentId
        navigate(AppRoute.DOCUMENTS)
    }

    /** Marks the current open-document request as handled. */
    fun consumeOpenDocumentRequest() {
        mutableOpenDocumentRequests.value = null
    }

    /** Navigates to the AI Suggested Update screen for [documentId]. */
    fun openAiUpdate(documentId: String) {
        mutableAiUpdateRequests.value = documentId
        navigate(AppRoute.AI_UPDATE)
    }

    /** Marks the current AI update request as handled. */
    fun consumeAiUpdateRequest() {
        mutableAiUpdateRequests.value = null
    }
}
