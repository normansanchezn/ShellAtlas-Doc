package com.shelldocs.core.domain.entity.updates

import kotlin.test.Test
import kotlin.test.assertEquals

class RiskLevelTest {

    @Test
    fun impactScoreBucketsMapToSeverities() {
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromImpactScore(94))
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromImpactScore(80))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromImpactScore(79))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromImpactScore(60))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromImpactScore(59))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromImpactScore(40))
        assertEquals(RiskLevel.LOW, RiskLevel.fromImpactScore(39))
        assertEquals(RiskLevel.LOW, RiskLevel.fromImpactScore(0))
    }

    @Test
    fun fromSignalsIsCriticalWhenStaleBeyondAYear() {
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromSignals(reviewAgeDays = 366, versionMismatch = false, hasUnreflectedUpstreamChanges = false))
        assertEquals(RiskLevel.LOW, RiskLevel.fromSignals(reviewAgeDays = 365, versionMismatch = false, hasUnreflectedUpstreamChanges = false))
    }

    @Test
    fun fromSignalsIsCriticalOnVersionMismatch() {
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromSignals(reviewAgeDays = 1, versionMismatch = true, hasUnreflectedUpstreamChanges = false))
    }

    @Test
    fun fromSignalsIsCriticalOnUnreflectedUpstreamChanges() {
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromSignals(reviewAgeDays = 1, versionMismatch = false, hasUnreflectedUpstreamChanges = true))
    }

    @Test
    fun fromSignalsIsLowWhenNoSignalsFire() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromSignals(reviewAgeDays = 1, versionMismatch = false, hasUnreflectedUpstreamChanges = false))
    }
}
