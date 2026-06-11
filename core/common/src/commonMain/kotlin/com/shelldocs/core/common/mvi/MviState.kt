package com.shelldocs.core.common.mvi

/**
 * Marker for immutable view state. Implementations must be data classes so
 * every reduction produces a new comparable snapshot.
 */
interface MviState
