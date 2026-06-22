package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.entity.onboarding.QuizQuestion

/** Tracks the guided knowledge-transfer checklist and how much of it is done. */
interface KnowledgeCheckpointRepository {

    suspend fun checkpoints(): DomainResult<List<KnowledgeCheckpoint>>

    suspend fun progress(): DomainResult<KnowledgeProgress>

    suspend fun complete(checkpointId: String): DomainResult<KnowledgeProgress>

    /** Quiz gating advancement past [checkpointId]; required before [complete] is called. */
    suspend fun quiz(checkpointId: String): DomainResult<List<QuizQuestion>>

    /** Grades [answers] (questionId -> chosen option index) and stores the attempt. */
    suspend fun submitQuiz(checkpointId: String, answers: Map<String, Int>): DomainResult<QuizAttempt>

    /** Every quiz attempt recorded so far, in submission order. */
    suspend fun quizAttempts(): DomainResult<List<QuizAttempt>>
}
