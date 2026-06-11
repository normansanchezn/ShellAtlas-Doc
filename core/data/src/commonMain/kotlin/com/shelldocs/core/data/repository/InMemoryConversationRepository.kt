package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.repository.ConversationRepository

/** Keeps assistant threads for the current session, newest first. */
class InMemoryConversationRepository(
    seed: List<Conversation> = emptyList(),
) : ConversationRepository {

    private val conversations = seed.toMutableList()

    override suspend fun conversations(): DomainResult<List<Conversation>> =
        DomainResult.success(conversations.sortedByDescending { it.updatedAt })

    override suspend fun upsert(conversation: Conversation): DomainResult<Unit> {
        conversations.removeAll { it.id == conversation.id }
        conversations += conversation
        return DomainResult.success(Unit)
    }

    override suspend fun delete(conversationId: String): DomainResult<Unit> {
        conversations.removeAll { it.id == conversationId }
        return DomainResult.success(Unit)
    }
}
