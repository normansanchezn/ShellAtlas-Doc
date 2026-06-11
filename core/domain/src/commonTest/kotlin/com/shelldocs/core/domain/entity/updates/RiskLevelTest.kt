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
}
