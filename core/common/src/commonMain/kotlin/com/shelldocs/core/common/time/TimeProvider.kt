package com.shelldocs.core.common.time

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Clock abstraction so staleness/health calculations are deterministic
 * in unit tests.
 */
fun interface TimeProvider {
    @OptIn(ExperimentalTime::class)
    fun now(): kotlin.time.Instant
}
