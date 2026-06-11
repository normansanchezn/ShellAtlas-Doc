package com.shelldocs.core.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction over coroutine dispatchers so view models and use cases can be
 * driven by virtual time in unit tests.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}
