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
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.usecase.assistant.AskAssistantUseCase
import com.shelldocs.core.domain.usecase.assistant.BuildWelcomeMessageUseCase
import com.shelldocs.core.domain.usecase.assistant.CheckAssistantAvailabilityUseCase
import com.shelldocs.core.domain.usecase.assistant.DetectAssistantLanguageUseCase
import com.shelldocs.core.domain.usecase.assistant.GetConversationsUseCase
import com.shelldocs.core.domain.usecase.assistant.SaveConversationUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import com.shelldocs.core.domain.usecase.onboarding.BuildKnowledgeTransferMessageUseCase
import com.shelldocs.core.domain.usecase.onboarding.CompleteKnowledgeCheckpointUseCase
import com.shelldocs.core.domain.usecase.onboarding.DetectCheckpointCompletionUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeCheckpointsUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeProgressUseCase
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class AssistantViewModel(
    private val askAssistant: AskAssistantUseCase,
    private val checkAvailability: CheckAssistantAvailabilityUseCase,
    private val getConversations: GetConversationsUseCase,
    private val saveConversation: SaveConversationUseCase,
    private val getDocuments: GetDocumentsUseCase,
    private val getKnowledgeCheckpoints: GetKnowledgeCheckpointsUseCase,
    private val getKnowledgeProgress: GetKnowledgeProgressUseCase,
    private val completeKnowledgeCheckpoint: CompleteKnowledgeCheckpointUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    dispatchers: DispatcherProvider,
    private val buildWelcomeMessage: BuildWelcomeMessageUseCase = BuildWelcomeMessageUseCase(),
    private val detectLanguage: DetectAssistantLanguageUseCase = DetectAssistantLanguageUseCase(),
    private val buildKnowledgeTransferMessage: BuildKnowledgeTransferMessageUseCase = BuildKnowledgeTransferMessageUseCase(),
    private val detectCheckpointCompletion: DetectCheckpointCompletionUseCase = DetectCheckpointCompletionUseCase(),
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
                        messages = listOf(welcomeMessage(AssistantLanguage.ENGLISH)),
                        errorDialog = null,
                        conversationLanguage = AssistantLanguage.ENGLISH,
                        activeCheckpointId = null,
                    )
                }
            AssistantIntent.StartKnowledgeTransfer -> startKnowledgeTransfer()
        }
    }

    private suspend fun initialize() {
        setState { copy(isInitializing = true, errorDialog = null) }
        val (availability, conversations, documents, checkpoints, progress) = coroutineScope {
            val availabilityDeferred = async(dispatchers.io) { checkAvailability() }
            val conversationsDeferred = async(dispatchers.io) { getConversations() }
            val documentsDeferred = async(dispatchers.io) { getDocuments() }
            val checkpointsDeferred = async(dispatchers.io) { getKnowledgeCheckpoints() }
            val progressDeferred = async(dispatchers.io) { getKnowledgeProgress() }
            ResultBundle(
                availabilityDeferred.await(),
                conversationsDeferred.await().getOrDefault(emptyList()),
                documentsDeferred.await().getOrDefault(emptyList()),
                checkpointsDeferred.await().getOrDefault(emptyList()),
                progressDeferred.await().getOrDefault(KnowledgeProgress(0, 0)),
            )
        }
        setState {
            copy(
                isInitializing = false,
                availability = availability,
                conversations = conversations,
                indexedDocuments = documents.size,
                messages = if (messages.isEmpty()) listOf(welcomeMessage()) else messages,
                checkpoints = checkpoints,
                knowledgeProgress = progress,
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun startKnowledgeTransfer() {
        val checkpoints = currentState.checkpoints
        val progress = currentState.knowledgeProgress ?: KnowledgeProgress(0, checkpoints.size)
        val language = currentState.conversationLanguage
        val next = checkpoints.getOrNull(progress.completed)
        val markdown = if (next != null) {
            buildKnowledgeTransferMessage.step(next, progress, language)
        } else {
            buildKnowledgeTransferMessage.completion(progress, language)
        }
        val assistantMessage = AssistantMessage(
            id = idGenerator.newId(),
            role = MessageRole.ASSISTANT,
            markdown = markdown,
            createdAt = timeProvider.now(),
        )
        setState { copy(messages = messages + assistantMessage, activeCheckpointId = next?.id) }
        sendEffect(AssistantEffect.ScrollToLatestMessage)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun advanceKnowledgeTransfer(completedCheckpointId: String) {
        val language = currentState.conversationLanguage
        val progress = withContext(dispatchers.io) { completeKnowledgeCheckpoint(completedCheckpointId) }
            .getOrDefault(currentState.knowledgeProgress ?: KnowledgeProgress(0, 0))
        val checkpoints = currentState.checkpoints
        val next = checkpoints.getOrNull(progress.completed)
        val markdown = if (next != null) {
            buildKnowledgeTransferMessage.step(next, progress, language)
        } else {
            buildKnowledgeTransferMessage.completion(progress, language)
        }
        val assistantMessage = AssistantMessage(
            id = idGenerator.newId(),
            role = MessageRole.ASSISTANT,
            markdown = markdown,
            createdAt = timeProvider.now(),
        )
        setState {
            copy(
                messages = messages + assistantMessage,
                knowledgeProgress = progress,
                activeCheckpointId = next?.id,
                isAnswering = false,
            )
        }
        sendEffect(AssistantEffect.ScrollToLatestMessage)
    }

    private data class ResultBundle(
        val availability: com.shelldocs.core.domain.entity.assistant.AssistantAvailability,
        val conversations: List<Conversation>,
        val documents: List<com.shelldocs.core.domain.entity.document.Document>,
        val checkpoints: List<com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint>,
        val progress: KnowledgeProgress,
    )

    @OptIn(ExperimentalTime::class)
    private fun welcomeMessage(language: AssistantLanguage = currentState.conversationLanguage): AssistantMessage = AssistantMessage(
        id = idGenerator.newId(),
        role = MessageRole.ASSISTANT,
        markdown = buildWelcomeMessage(language),
        createdAt = timeProvider.now(),
    )

    private suspend fun select(conversationId: String) {
        val conversation = currentState.conversations.firstOrNull { it.id == conversationId } ?: return
        val language = conversation.messages
            .lastOrNull { it.role == MessageRole.USER }
            ?.markdown
            ?.let { detectLanguage(it, default = currentState.conversationLanguage) }
            ?: currentState.conversationLanguage
        setState {
            copy(
                activeConversationId = conversation.id,
                messages = conversation.messages,
                errorDialog = null,
                conversationLanguage = language,
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
        val activeCheckpointId = currentState.activeCheckpointId
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

        if (activeCheckpointId != null && detectCheckpointCompletion(question)) {
            advanceKnowledgeTransfer(activeCheckpointId)
            return
        }

        withContext(dispatchers.default) {
            askAssistant(question, language)
        }
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
        withContext(dispatchers.io) {
            saveConversation(conversation)
        }
        val refreshed = withContext(dispatchers.io) {
            getConversations().getOrDefault(snapshot.conversations)
        }
        setState { copy(activeConversationId = conversation.id, conversations = refreshed) }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 38
    }
}
