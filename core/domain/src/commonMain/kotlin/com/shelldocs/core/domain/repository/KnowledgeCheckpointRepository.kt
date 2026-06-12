package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress

/** Tracks the guided knowledge-transfer checklist and how much of it is done. */
interface KnowledgeCheckpointRepository {

    suspend fun checkpoints(): DomainResult<List<KnowledgeCheckpoint>>

    suspend fun progress(): DomainResult<KnowledgeProgress>

    suspend fun complete(checkpointId: String): DomainResult<KnowledgeProgress>
}
