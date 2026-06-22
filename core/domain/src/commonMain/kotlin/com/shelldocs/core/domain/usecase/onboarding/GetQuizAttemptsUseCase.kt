package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository

class GetQuizAttemptsUseCase(private val repository: KnowledgeCheckpointRepository) {

    suspend operator fun invoke(): DomainResult<List<QuizAttempt>> = repository.quizAttempts()
}
