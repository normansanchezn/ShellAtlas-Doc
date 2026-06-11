package com.shelldocs.core.common.mvi

import com.shelldocs.core.common.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class of the unidirectional MVI loop:
 *
 * UI -> [onIntent] -> [handleIntent] -> [setState] -> UI
 *                                    -> [sendEffect] -> one-shot consumption
 *
 * It is framework-agnostic on purpose so presentation logic stays fully unit
 * testable on every Kotlin Multiplatform target.
 */
abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect>(
    initialState: S,
    protected val dispatchers: DispatcherProvider,
) {
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.main)

    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<E>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: Flow<E> = mutableEffects.asSharedFlow()

    val currentState: S get() = mutableState.value

    /** Single entry point used by the UI to dispatch intents. */
    fun onIntent(intent: I) {
        scope.launch { handleIntent(intent) }
    }

    /** Translates an intent into state reductions and/or effects. */
    protected abstract suspend fun handleIntent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        mutableState.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        mutableEffects.tryEmit(effect)
    }

    /** Releases every coroutine owned by this view model. */
    fun clear() {
        onCleared()
        scope.cancel()
    }

    protected open fun onCleared() = Unit
}
