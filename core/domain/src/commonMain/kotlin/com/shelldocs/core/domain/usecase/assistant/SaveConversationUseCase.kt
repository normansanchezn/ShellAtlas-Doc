package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.repository.ConversationRepository

class SaveConversationUseCase(private val conversationRepository: ConversationRepository) {

    suspend operator fun invoke(conversation: Conversation): DomainResult<Unit> =
        conversationRepository.upsert(conversation)
}
