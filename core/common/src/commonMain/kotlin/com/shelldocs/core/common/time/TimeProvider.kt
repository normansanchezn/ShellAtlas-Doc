package com.shelldocs.core.common.time

import kotlinx.datetime.Instant

/**
 * Clock abstraction so staleness/health calculations are deterministic
 * in unit tests.
 */
fun interface TimeProvider {
    fun now(): Instant
}
