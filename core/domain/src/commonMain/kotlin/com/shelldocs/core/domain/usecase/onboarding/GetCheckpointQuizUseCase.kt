package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.QuizQuestion
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class GetCheckpointQuizUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(checkpointId: String): DomainResult<List<QuizQuestion>> =
        repository.quiz(checkpointId)
}
