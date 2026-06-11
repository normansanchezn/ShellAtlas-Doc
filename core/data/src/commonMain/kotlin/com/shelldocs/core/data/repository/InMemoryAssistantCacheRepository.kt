package com.shelldocs.core.data.repository

import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.repository.AssistantCacheRepository

/**
 * Session-scoped answer cache. The Supabase `assistant_intelligence` table
 * provides the durable tier; this keeps repeat questions instant offline.
 */
class InMemoryAssistantCacheRepository : AssistantCacheRepository {

    private val entries = mutableMapOf<String, AssistantAnswer>()

    override suspend fun lookup(questionHash: String, keywords: List<String>): AssistantAnswer? =
        entries[questionHash]

    override suspend fun save(questionHash: String, keywords: List<String>, answer: AssistantAnswer) {
        entries[questionHash] = answer
    }
}
