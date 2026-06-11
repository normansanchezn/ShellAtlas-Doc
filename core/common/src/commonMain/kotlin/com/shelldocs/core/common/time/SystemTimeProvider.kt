package com.shelldocs.core.common.time

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Production [TimeProvider] backed by the system clock. */

class SystemTimeProvider : TimeProvider {
    @OptIn(ExperimentalTime::class)
    override fun now(): kotlin.time.Instant = kotlin.time.Clock.System.now()
}
