package com.shelldocs.core.domain.entity.dashboard

/** Document coverage for one [com.shelldocs.core.domain.entity.document.Area]. */
data class AreaCoverage(
    val area: String,
    val documentCount: Int,
    val healthyPercent: Int,
)
