package com.shelldocs.core.common.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Production [TimeProvider] backed by the system clock. */
class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
