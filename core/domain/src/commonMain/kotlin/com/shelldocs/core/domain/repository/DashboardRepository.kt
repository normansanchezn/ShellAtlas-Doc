package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Aggregated knowledge-operations metrics. */
interface DashboardRepository {

    suspend fun metrics(): DomainResult<DashboardMetrics>
}
