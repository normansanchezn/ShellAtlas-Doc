package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class SubmitCheckpointQuizUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(checkpointId: String, answers: Map<String, Int>): DomainResult<QuizAttempt> =
        repository.submitQuiz(checkpointId, answers)
}
