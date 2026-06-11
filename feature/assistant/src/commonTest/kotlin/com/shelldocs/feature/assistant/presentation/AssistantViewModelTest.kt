@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.id.IdGenerator
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.Conversation
import com.shelldocs.core.domain.entity.assistant.MessageRole
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentContent
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.AssistantCacheRepository
import com.shelldocs.core.domain.repository.AssistantEngine
import com.shelldocs.core.domain.repository.ConversationRepository
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.usecase.assistant.AskAssistantUseCase
import com.shelldocs.core.domain.usecase.assistant.CheckAssistantAvailabilityUseCase
import com.shelldocs.core.domain.usecase.assistant.CreateDocumentFromAssistantUseCase
import com.shelldocs.core.domain.usecase.assistant.DetectAssistantIntentUseCase
import com.shelldocs.core.domain.usecase.assistant.GetConversationsUseCase
import com.shelldocs.core.domain.usecase.assistant.RetrieveGroundingDocumentsUseCase
import com.shelldocs.core.domain.usecase.assistant.SaveConversationUseCase
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private class FixedEngine(var failNext: Boolean = false) : AssistantEngine {
    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): DomainResult<AssistantAnswer> =
        if (failNext) {
            DomainResult.failure(AppError.Network("Assistant offline"))
        } else {
            DomainResult.success(
                AssistantAnswer(
                    markdown = "Grounded answer about $question",
                    confidence = AnswerConfidence.HIGH,
                    sources = emptyList(),
                    intent = intent,
                ),
            )
        }

    override suspend fun availability() =
        AssistantAvailability(isLlmReachable = false, modelName = null, statusMessage = "grounded")
}

private class NoopCache : AssistantCacheRepository {
    override suspend fun lookup(questionHash: String, keywords: List<String>): AssistantAnswer? = null
    override suspend fun save(questionHash: String, keywords: List<String>, answer: AssistantAnswer) = Unit
}

private class TestConversationRepository : ConversationRepository {
    private val conversations = mutableListOf<Conversation>()

    override suspend fun conversations(): DomainResult<List<Conversation>> =
        DomainResult.success(conversations.toList())

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

private class SingleDocumentRepository : DocumentRepository {
    private val document = Document(
        id = "doc-auth",
        title = "Authentication",
        summary = "Token behavior",
        status = DocumentStatus.PUBLISHED,
        classification = DocumentClassification.INTERNAL,
        rawMarkdown = "# Authentication\n\nTokens rotate silently.",
        content = DocumentContent(),
        plainText = "Authentication Tokens rotate silently.",
        attributes = DocumentAttributes(module = "Authentication"),
        createdAt = Instant.parse("2026-06-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-06-01T00:00:00Z"),
    )

    override suspend fun documents() = DomainResult.success(listOf(document))
    override suspend fun document(id: String) = DomainResult.success(document)
    override suspend fun search(query: String) = DomainResult.success(listOf(document))
    override suspend fun create(title: String, markdown: String, parentFolderId: String?) =
        DomainResult.success(document)
    override suspend fun publish(id: String, markdown: String, changeSummary: String) =
        DomainResult.success(document)
    override suspend fun saveDraft(id: String, markdown: String) =
        DomainResult.success(DraftReceipt(id, "hash", Instant.parse("2026-06-01T00:00:00Z")))
    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> =
        DomainResult.success(emptyList())
    override suspend fun restoreVersion(id: String, versionId: String) = DomainResult.success(document)
    override suspend fun updateAttributes(id: String, attributes: DocumentAttributes) =
        DomainResult.success(document.copy(attributes = attributes))
    override suspend fun delete(id: String) = DomainResult.success(Unit)
}

class AssistantViewModelTest {

    private val engine = FixedEngine()

    private fun viewModel(scheduler: TestCoroutineScheduler): AssistantViewModel {
        val documents = SingleDocumentRepository()
        val conversations = TestConversationRepository()
        var idCounter = 0
        return AssistantViewModel(
            askAssistant = AskAssistantUseCase(
                detectIntent = DetectAssistantIntentUseCase(),
                retrieveGroundingDocuments = RetrieveGroundingDocumentsUseCase(documents),
                engine = engine,
                cache = NoopCache(),
                createDocumentFromAssistant = CreateDocumentFromAssistantUseCase(CreateDocumentUseCase(documents)),
                roleProvider = { UserRole.DEVELOP },
            ),
            checkAvailability = CheckAssistantAvailabilityUseCase(engine),
            getConversations = GetConversationsUseCase(conversations),
            saveConversation = SaveConversationUseCase(conversations),
            getDocuments = GetDocumentsUseCase(documents),
            timeProvider = TimeProvider { Instant.parse("2026-06-11T10:00:00Z") },
            idGenerator = IdGenerator { "id-${++idCounter}" },
            dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
        )
    }

    @Test
    fun initializeLoadsAvailabilityAndIndexCount() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.availability)
        assertEquals(1, viewModel.currentState.indexedDocuments)
        viewModel.clear()
    }

    @Test
    fun initializeShowsWelcomeMessageWhenNoMessagesYet() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.Initialize)
        testScheduler.advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(1, state.messages.size)
        assertEquals(MessageRole.ASSISTANT, state.messages[0].role)
        assertTrue(state.messages[0].markdown.isNotBlank())
        viewModel.clear()
    }

    @Test
    fun sendingAppendsUserAndAssistantMessages() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.InputChanged("How does authentication work?"))
        viewModel.onIntent(AssistantIntent.SendQuestion)
        testScheduler.advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(2, state.messages.size)
        assertEquals(MessageRole.USER, state.messages[0].role)
        assertEquals(MessageRole.ASSISTANT, state.messages[1].role)
        assertEquals(AnswerConfidence.HIGH, state.messages[1].confidence)
        assertEquals("", state.input)
        assertFalse(state.isAnswering)
        viewModel.clear()
    }

    @Test
    fun engineFailureKeepsUserMessageAndShowsError() = runTest {
        engine.failNext = true
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.InputChanged("Anything"))
        viewModel.onIntent(AssistantIntent.SendQuestion)
        testScheduler.advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(1, state.messages.size)
        assertEquals("We couldn't complete your request", state.errorDialog?.title)
        viewModel.clear()
    }

    @Test
    fun newConversationClearsThread() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.InputChanged("How does authentication work?"))
        viewModel.onIntent(AssistantIntent.SendQuestion)
        testScheduler.advanceUntilIdle()
        viewModel.onIntent(AssistantIntent.StartNewConversation)
        testScheduler.advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(1, state.messages.size)
        assertEquals(MessageRole.ASSISTANT, state.messages[0].role)
        assertEquals(null, state.activeConversationId)
        viewModel.clear()
    }

    @Test
    fun blankInputCannotBeSent() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(AssistantIntent.InputChanged("   "))
        viewModel.onIntent(AssistantIntent.SendQuestion)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.currentState.messages.isEmpty())
        viewModel.clear()
    }
}
