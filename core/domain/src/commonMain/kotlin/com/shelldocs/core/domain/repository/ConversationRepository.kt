package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.Conversation

/** Persistence of assistant threads shown in the conversations panel. */
interface ConversationRepository {

    suspend fun conversations(): DomainResult<List<Conversation>>

    suspend fun upsert(conversation: Conversation): DomainResult<Unit>

    suspend fun delete(conversationId: String): DomainResult<Unit>
}
