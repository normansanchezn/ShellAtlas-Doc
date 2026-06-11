package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.id.IdGenerator
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.AssistantMessage
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.entity.assistant.MessageRole
import com.shelldocs.core.domain.usecase.assistant.AskAssistantUseCase
import com.shelldocs.core.domain.usecase.assistant.BuildWelcomeMessageUseCase
import com.shelldocs.core.domain.usecase.assistant.CheckAssistantAvailabilityUseCase
import com.shelldocs.core.domain.usecase.assistant.DetectAssistantLanguageUseCase
import com.shelldocs.core.domain.usecase.assistant.GetConversationsUseCase
import com.shelldocs.core.domain.usecase.assistant.SaveConversationUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import kotlin.time.ExperimentalTime

class AssistantViewModel(
    private val askAssistant: AskAssistantUseCase,
    private val checkAvailability: CheckAssistantAvailabilityUseCase,
    private val getConversations: GetConversationsUseCase,
    private val saveConversation: SaveConversationUseCase,
    private val getDocuments: GetDocumentsUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    dispatchers: DispatcherProvider,
    private val buildWelcomeMessage: BuildWelcomeMessageUseCase = BuildWelcomeMessageUseCase(),
    private val detectLanguage: DetectAssistantLanguageUseCase = DetectAssistantLanguageUseCase(),
) : MviViewModel<AssistantIntent, AssistantState, AssistantEffect>(AssistantState(), dispatchers) {

    override suspend fun handleIntent(intent: AssistantIntent) {
        when (intent) {
            AssistantIntent.Initialize -> initialize()
            is AssistantIntent.InputChanged -> setState { copy(input = intent.value) }
            AssistantIntent.SendQuestion -> send()
            is AssistantIntent.SelectConversation -> select(intent.conversationId)
            AssistantIntent.DismissError -> setState { copy(errorDialog = null) }
            AssistantIntent.StartNewConversation ->
                setState {
                    copy(
                        activeConversationId = null,
                        messages = listOf(welcomeMessage()),
                        errorDialog = null,
                        conversationLanguage = AssistantLanguage.SPANISH,
                    )
                }
        }
    }

    private suspend fun initialize() {
        setState { copy(isInitializing = true, errorDialog = null) }
        val availability = checkAvailability()
        val conversations = getConversations().getOrDefault(emptyList())
        val documents = getDocuments().getOrDefault(emptyList())
        setState {
            copy(
                isInitializing = false,
                availability = availability,
                conversations = conversations,
                indexedDocuments = documents.size,
                messages = if (messages.isEmpty()) listOf(welcomeMessage()) else messages,
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun welcomeMessage(): AssistantMessage = AssistantMessage(
        id = idGenerator.newId(),
        role = MessageRole.ASSISTANT,
        markdown = buildWelcomeMessage(),
        createdAt = timeProvider.now(),
    )

    private suspend fun select(conversationId: String) {
        val conversation = currentState.conversations.firstOrNull { it.id == conversationId } ?: return
        setState {
            copy(
                activeConversationId = conversation.id,
                messages = conversation.messages,
                errorDialog = null,
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun send() {
        val question = currentState.input.trim()
        if (question.isEmpty() || currentState.isAnswering) return

        val userMessage = AssistantMessage(
            id = idGenerator.newId(),
            role = MessageRole.USER,
            markdown = question,
            createdAt = timeProvider.now(),
        )
        val language = detectLanguage(question, default = currentState.conversationLanguage)
        setState {
            copy(
                messages = messages + userMessage,
                input = "",
                isAnswering = true,
                errorDialog = null,
                conversationLanguage = language,
            )
        }
        sendEffect(AssistantEffect.ScrollToLatestMessage)

        askAssistant(question, language)
            .onSuccess { answer ->
                val assistantMessage = AssistantMessage(
                    id = idGenerator.newId(),
                    role = MessageRole.ASSISTANT,
                    markdown = answer.markdown,
                    confidence = answer.confidence,
                    sources = answer.sources,
                    createdAt = timeProvider.now(),
                )
                setState { copy(messages = messages + assistantMessage, isAnswering = false) }
                sendEffect(AssistantEffect.ScrollToLatestMessage)
                persistActiveConversation(question)
            }
            .onFailure { error ->
                setState {
                    copy(
                        isAnswering = false,
                        errorDialog = error.toErrorDialogState("generate an answer"),
                    )
                }
            }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun persistActiveConversation(question: String) {
        val snapshot = currentState
        val conversation = Conversation(
            id = snapshot.activeConversationId ?: idGenerator.newId(),
            title = snapshot.conversations
                .firstOrNull { it.id == snapshot.activeConversationId }?.title
                ?: question.take(MAX_TITLE_LENGTH),
            messages = snapshot.messages,
            updatedAt = timeProvider.now(),
        )
        saveConversation(conversation)
        val refreshed = getConversations().getOrDefault(snapshot.conversations)
        setState { copy(activeConversationId = conversation.id, conversations = refreshed) }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 38
    }
}
