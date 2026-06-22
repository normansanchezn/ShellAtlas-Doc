package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.id.IdGenerator
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.persistence.SessionPreferences
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.AssistantMessage
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.entity.assistant.MessageRole
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.usecase.assistant.*
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import com.shelldocs.core.domain.usecase.onboarding.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class AssistantViewModel(
    private val askAssistant: AskAssistantUseCase,
    private val checkAvailability: CheckAssistantAvailabilityUseCase,
    private val getConversations: GetConversationsUseCase,
    private val saveConversation: SaveConversationUseCase,
    private val getDocuments: GetDocumentsUseCase,
    private val getKnowledgeCheckpoints: GetKnowledgeCheckpointsUseCase,
    private val getKnowledgeProgress: GetKnowledgeProgressUseCase,
    private val completeKnowledgeCheckpoint: CompleteKnowledgeCheckpointUseCase,
    private val getCheckpointQuiz: GetCheckpointQuizUseCase,
    private val submitCheckpointQuiz: SubmitCheckpointQuizUseCase,
    private val getQuizAttempts: GetQuizAttemptsUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    private val sessionPrefs: SessionPreferences,
    dispatchers: DispatcherProvider,
    private val buildWelcomeMessage: BuildWelcomeMessageUseCase = BuildWelcomeMessageUseCase(),
    private val detectLanguage: DetectAssistantLanguageUseCase = DetectAssistantLanguageUseCase(),
    private val buildKnowledgeTransferMessage: BuildKnowledgeTransferMessageUseCase = BuildKnowledgeTransferMessageUseCase(),
    private val detectCheckpointCompletion: DetectCheckpointCompletionUseCase = DetectCheckpointCompletionUseCase(),
    private val buildQuizMessage: BuildQuizMessageUseCase = BuildQuizMessageUseCase(),
    private val parseQuizAnswers: ParseQuizAnswersUseCase = ParseQuizAnswersUseCase(),
) : MviViewModel<AssistantIntent, AssistantState, AssistantEffect>(AssistantState(), dispatchers) {

    override suspend fun handleIntent(intent: AssistantIntent) {
        when (intent) {
            AssistantIntent.Initialize -> initialize()
            is AssistantIntent.InputChanged -> setState { copy(input = intent.value) }
            AssistantIntent.SendQuestion -> send()
            is AssistantIntent.SelectConversation -> select(intent.conversationId)
            AssistantIntent.DismissError -> setState { copy(errorDialog = null) }
            AssistantIntent.StartNewConversation -> {
                setState {
                    copy(
                        activeConversationId = null,
                        messages = listOf(welcomeMessage(AssistantLanguage.ENGLISH)),
                        errorDialog = null,
                        conversationLanguage = AssistantLanguage.ENGLISH,
                        activeCheckpointId = null,
                    )
                }
                sessionPrefs.saveAssistantConversationId(NEW_CONVERSATION_SENTINEL)
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
        val restoredConversation = resolveInitialConversation(conversations)
        setState {
            copy(
                isInitializing = false,
                availability = availability,
                conversations = conversations,
                indexedDocuments = documents.size,
                messages = restoredConversation?.messages
                    ?: if (messages.isEmpty()) listOf(welcomeMessage()) else messages,
                checkpoints = checkpoints,
                knowledgeProgress = progress,
                activeConversationId = restoredConversation?.id,
                conversationLanguage = restoredConversation?.let { resolveConversationLanguage(it) }
                    ?: conversationLanguage,
                activeCheckpointId = if (restoredConversation == null) activeCheckpointId else null,
            )
        }
        restoredConversation?.id?.let(sessionPrefs::saveAssistantConversationId)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun startKnowledgeTransfer() {
        val checkpoints = currentState.checkpoints
        val progress = currentState.knowledgeProgress ?: KnowledgeProgress(0, checkpoints.size)
        // KT flow always speaks English for now, regardless of detected conversation language.
        val language = AssistantLanguage.ENGLISH
        val next = checkpoints.getOrNull(progress.completed)
        val markdown = if (next != null) {
            buildKnowledgeTransferMessage.step(next, progress, language)
        } else {
            val attempts = withContext(dispatchers.io) { getQuizAttempts() }.getOrDefault(emptyList())
            buildKnowledgeTransferMessage.completion(progress, attempts, language)
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
    private suspend fun startQuizForCheckpoint(checkpointId: String) {
        // KT flow always speaks English for now, regardless of detected conversation language.
        val language = AssistantLanguage.ENGLISH
        val questions = withContext(dispatchers.io) { getCheckpointQuiz(checkpointId) }.getOrDefault(emptyList())
        if (questions.isEmpty()) {
            // No quiz defined for this checkpoint — fall back to advancing directly.
            advanceKnowledgeTransfer(checkpointId)
            return
        }
        val assistantMessage = AssistantMessage(
            id = idGenerator.newId(),
            role = MessageRole.ASSISTANT,
            markdown = buildQuizMessage.prompt(questions, language),
            createdAt = timeProvider.now(),
        )
        setState { copy(messages = messages + assistantMessage, activeQuiz = questions, isAnswering = false) }
        sendEffect(AssistantEffect.ScrollToLatestMessage)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun submitQuizAnswers(checkpointId: String, answers: Map<String, Int>) {
        // KT flow always speaks English for now, regardless of detected conversation language.
        val language = AssistantLanguage.ENGLISH
        val attempt = withContext(dispatchers.io) { submitCheckpointQuiz(checkpointId, answers) }
            .getOrDefault(
                QuizAttempt(
                    checkpointId = checkpointId,
                    correct = 0,
                    total = answers.size,
                    submittedAt = timeProvider.now(),
                ),
            )
        val feedbackMessage = AssistantMessage(
            id = idGenerator.newId(),
            role = MessageRole.ASSISTANT,
            markdown = buildQuizMessage.feedback(attempt, language),
            createdAt = timeProvider.now(),
        )
        setState { copy(messages = messages + feedbackMessage) }
        if (attempt.passed) {
            setState { copy(activeQuiz = emptyList()) }
            advanceKnowledgeTransfer(checkpointId)
        } else {
            setState { copy(isAnswering = false) }
            sendEffect(AssistantEffect.ScrollToLatestMessage)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun advanceKnowledgeTransfer(completedCheckpointId: String) {
        // KT flow always speaks English for now, regardless of detected conversation language.
        val language = AssistantLanguage.ENGLISH
        val progress = withContext(dispatchers.io) { completeKnowledgeCheckpoint(completedCheckpointId) }
            .getOrDefault(currentState.knowledgeProgress ?: KnowledgeProgress(0, 0))
        val checkpoints = currentState.checkpoints
        val next = checkpoints.getOrNull(progress.completed)
        val markdown = if (next != null) {
            buildKnowledgeTransferMessage.step(next, progress, language)
        } else {
            val attempts = withContext(dispatchers.io) { getQuizAttempts() }.getOrDefault(emptyList())
            buildKnowledgeTransferMessage.completion(progress, attempts, language)
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
                activeQuiz = emptyList(),
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
        isWelcome = true,
    )

    private suspend fun select(conversationId: String) {
        val conversation = currentState.conversations.firstOrNull { it.id == conversationId } ?: return
        val language = conversationLanguage(conversation)
        setState {
            copy(
                activeConversationId = conversation.id,
                messages = conversation.messages,
                errorDialog = null,
                conversationLanguage = language,
            )
        }
        sessionPrefs.saveAssistantConversationId(conversation.id)
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

        val activeQuiz = currentState.activeQuiz
        if (activeQuiz.isNotEmpty() && activeCheckpointId != null) {
            val answers = parseQuizAnswers(question, activeQuiz)
            if (answers != null) {
                submitQuizAnswers(activeCheckpointId, answers)
                return
            }
            // Not a quiz answer (e.g. a real question) — answer normally below, quiz stays active.
        } else if (activeCheckpointId != null && detectCheckpointCompletion(question)) {
            startQuizForCheckpoint(activeCheckpointId)
            return
        }

        val conversationMessages = currentState.messages
        withContext(dispatchers.default) {
            askAssistant(question, conversationMessages, language)
        }
            .onSuccess { answer ->
                val assistantMessage = AssistantMessage(
                    id = idGenerator.newId(),
                    role = MessageRole.ASSISTANT,
                    markdown = answer.markdown,
                    confidence = answer.confidence,
                    sources = answer.sources,
                    createdAt = timeProvider.now(),
                    isUnavailable = answer.isUnavailable,
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
        sessionPrefs.saveAssistantConversationId(conversation.id)
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 38
        const val NEW_CONVERSATION_SENTINEL = "__assistant_new_conversation__"
    }

    private fun resolveInitialConversation(conversations: List<Conversation>): Conversation? {
        val persistedId = sessionPrefs.loadAssistantConversationId()
        if (persistedId == NEW_CONVERSATION_SENTINEL) return null
        if (persistedId != null) {
            conversations.firstOrNull { it.id == persistedId }?.let { return it }
        }
        return conversations.firstOrNull()
    }

    private fun conversationLanguage(
        conversation: Conversation,
        fallback: AssistantLanguage = currentState.conversationLanguage,
    ): AssistantLanguage =
        conversation.messages
            .lastOrNull { it.role == MessageRole.USER }
            ?.markdown
            ?.let { detectLanguage(it, default = fallback) }
            ?: fallback

    private fun resolveConversationLanguage(
        conversation: Conversation,
        fallback: AssistantLanguage = currentState.conversationLanguage,
    ): AssistantLanguage = conversationLanguage(conversation, fallback)
}
