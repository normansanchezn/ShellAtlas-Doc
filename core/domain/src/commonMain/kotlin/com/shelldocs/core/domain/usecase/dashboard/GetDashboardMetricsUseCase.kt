package com.shelldocs.core.domain.usecase.dashboard

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.core.domain.repository.DashboardRepository

class GetDashboardMetricsUseCase(private val dashboardRepository: DashboardRepository) {

    suspend operator fun invoke(): DomainResult<DashboardMetrics> = dashboardRepository.metrics()
}
