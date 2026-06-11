package com.shelldocs.core.domain.usecase.updates

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.repository.PendingUpdatesRepository

class ScanForUpdatesUseCase(private val pendingUpdatesRepository: PendingUpdatesRepository) {

    suspend operator fun invoke(): DomainResult<List<PendingUpdate>> =
        pendingUpdatesRepository.scanNow().map { updates ->
            updates.sortedWith(compareBy({ it.risk }, { -it.impactScore }))
        }
}
