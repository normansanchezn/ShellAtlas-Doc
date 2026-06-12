package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class GetKnowledgeProgressUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(): DomainResult<KnowledgeProgress> = repository.progress()
}
