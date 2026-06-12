package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class CompleteKnowledgeCheckpointUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(checkpointId: String): DomainResult<KnowledgeProgress> =
        repository.complete(checkpointId)
}
