@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.fixtures

import com.shelldocs.core.common.time.TimeProvider
import kotlinx.datetime.Instant

class FixedTimeProvider(private val fixed: Instant = DocumentFixtures.baseInstant) : TimeProvider {
    override fun now(): Instant = fixed
}
