package com.shelldocs.core.domain.entity.dashboard

/** Documentation coverage per app module (0-100). */
data class ModuleCoverage(
    val module: String,
    val coveragePercent: Int,
)
