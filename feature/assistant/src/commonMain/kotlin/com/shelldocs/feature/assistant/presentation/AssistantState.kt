package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.AssistantMessage
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress

/** Snapshot of the AI Assistant screen. */
data class AssistantState(
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<AssistantMessage> = emptyList(),
    val input: String = "",
    val isInitializing: Boolean = false,
    val isAnswering: Boolean = false,
    val availability: AssistantAvailability? = null,
    val indexedDocuments: Int = 0,
    val errorDialog: ErrorDialogState? = null,
    val conversationLanguage: AssistantLanguage = AssistantLanguage.SPANISH,
    val checkpoints: List<KnowledgeCheckpoint> = emptyList(),
    val activeCheckpointId: String? = null,
    val knowledgeProgress: KnowledgeProgress? = null,
) : MviState {
    val canSend: Boolean = input.isNotBlank() && !isAnswering
}
