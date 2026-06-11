package com.shelldocs.core.domain.usecase.source

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.repository.SourcesRepository

class GetSourcesUseCase(private val sourcesRepository: SourcesRepository) {

    suspend operator fun invoke(): DomainResult<List<KnowledgeSource>> = sourcesRepository.sources()
}
