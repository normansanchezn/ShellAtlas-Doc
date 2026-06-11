package com.shelldocs.core.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Production dispatchers. `io` falls back to [Dispatchers.Default] because
 * Kotlin/Wasm and Kotlin/Native expose no dedicated IO pool in common code;
 * Ktor performs its own off-main networking on every target.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.Default
}
