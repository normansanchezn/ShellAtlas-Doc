package com.shelldocs.core.domain.usecase.source

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.source.SyncLogEntry
import com.shelldocs.core.domain.repository.SourcesRepository

class GetSyncLogUseCase(private val sourcesRepository: SourcesRepository) {

    suspend operator fun invoke(): DomainResult<List<SyncLogEntry>> = sourcesRepository.syncLog()
}
