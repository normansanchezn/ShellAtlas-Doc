package com.shelldocs.core.domain.repository

import com.shelldocs.core.domain.entity.assistant.AssistantAnswer

/**
 * Answer cache backed by the Supabase `assistant_intelligence` table so
 * repeated questions are served instantly and offline.
 */
interface AssistantCacheRepository {

    suspend fun lookup(questionHash: String, keywords: List<String>): AssistantAnswer?

    suspend fun save(questionHash: String, keywords: List<String>, answer: AssistantAnswer)
}
