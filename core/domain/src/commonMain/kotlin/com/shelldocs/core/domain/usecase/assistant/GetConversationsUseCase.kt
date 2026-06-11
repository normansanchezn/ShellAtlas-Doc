package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.repository.ConversationRepository

class GetConversationsUseCase(private val conversationRepository: ConversationRepository) {

    suspend operator fun invoke(): DomainResult<List<Conversation>> =
        conversationRepository.conversations()
}
