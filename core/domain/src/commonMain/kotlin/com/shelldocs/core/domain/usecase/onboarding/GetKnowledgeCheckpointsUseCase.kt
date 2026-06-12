package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class GetKnowledgeCheckpointsUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(): DomainResult<List<KnowledgeCheckpoint>> = repository.checkpoints()
}
