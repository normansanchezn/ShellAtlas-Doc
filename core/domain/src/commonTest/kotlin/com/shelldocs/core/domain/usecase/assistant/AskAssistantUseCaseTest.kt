@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.domain.entity.assistant.*
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import com.shelldocs.core.domain.repository.AssistantCacheRepository
import com.shelldocs.core.domain.repository.AssistantEngine
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.*

private class RecordingEngine : AssistantEngine {
    var invocations = 0
    var lastIntent: AssistantIntentType? = null
    var lastGrounding: List<ScoredDocument> = emptyList()
    var lastQuestion: String? = null

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        groundingDocuments: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): DomainResult<AssistantAnswer> {
        invocations++
        lastIntent = intent
        lastGrounding = groundingDocuments
        lastQuestion = question
        return DomainResult.success(
            AssistantAnswer(
                markdown = "Grounded answer",
                confidence = AnswerConfidence.HIGH,
                sources = emptyList(),
                intent = intent,
            ),
        )
    }

    override suspend fun availability() = AssistantAvailability(
        isLlmReachable = false,
        modelName = null,
        statusMessage = "grounded fallback",
    )
}

private class InMemoryCache : AssistantCacheRepository {
    val entries = mutableMapOf<String, AssistantAnswer>()

    override suspend fun lookup(questionHash: String, keywords: List<String>): AssistantAnswer? =
        entries[questionHash]

    override suspend fun save(questionHash: String, keywords: List<String>, answer: AssistantAnswer) {
        entries[questionHash] = answer
    }
}

class AskAssistantUseCaseTest {

    private val engine = RecordingEngine()
    private val cache = InMemoryCache()
    private val repository = FakeDocumentRepository(
        stored = listOf(DocumentFixtures.document(id = "auth", title = "Authentication")),
    )
    private val useCase = AskAssistantUseCase(
        detectIntent = DetectAssistantIntentUseCase(),
        retrieveGroundingDocuments = RetrieveGroundingDocumentsUseCase(repository),
        engine = engine,
        cache = cache,
        createDocumentFromAssistant = CreateDocumentFromAssistantUseCase(CreateDocumentUseCase(repository)),
        roleProvider = { UserRole.DEVELOP },
    )

    @Test
    fun firstAskHitsEngineAndPopulatesCache() = runTest {
        val answer = useCase("How does authentication refresh work?").getOrNull()

        assertNotNull(answer)
        assertFalse(answer.fromCache)
        assertEquals(1, engine.invocations)
        assertEquals(1, cache.entries.size)
    }

    @Test
    fun repeatedQuestionIsServedFromCache() = runTest {
        useCase("How does authentication refresh work?")
        val second = useCase("How does authentication refresh work?").getOrNull()

        assertNotNull(second)
        assertTrue(second.fromCache)
        assertEquals(1, engine.invocations)
    }

    @Test
    fun intentAndGroundingAreForwardedToEngine() = runTest {
        useCase("Explain the authentication flow step by step")

        assertEquals(AssistantIntentType.EXPLAIN_FLOW, engine.lastIntent)
        assertEquals(listOf("auth"), engine.lastGrounding.map { it.document.id })
    }

    @Test
    fun followUpQuestionUsesPriorChatContext() = runTest {
        val history = listOf(
            AssistantMessage(
                id = "msg-1",
                role = MessageRole.USER,
                markdown = "We are on the KT for the authentication flow.",
                createdAt = kotlinx.datetime.Instant.parse("2026-06-11T10:00:00Z"),
            ),
            AssistantMessage(
                id = "msg-2",
                role = MessageRole.ASSISTANT,
                markdown = "Next we review token refresh and sign-out.",
                createdAt = kotlinx.datetime.Instant.parse("2026-06-11T10:01:00Z"),
            ),
        )

        useCase("what next?", conversationMessages = history)

        assertNotNull(engine.lastQuestion)
        assertTrue(engine.lastQuestion!!.contains("Conversation context:"))
        assertTrue(engine.lastQuestion!!.contains("authentication flow"))
        assertTrue(engine.lastQuestion!!.contains("Current question:"))
    }

    @Test
    fun explicitTopicShiftIgnoresPriorChatContext() = runTest {
        val history = listOf(
            AssistantMessage(
                id = "msg-1",
                role = MessageRole.USER,
                markdown = "We are on the KT for the authentication flow.",
                createdAt = kotlinx.datetime.Instant.parse("2026-06-11T10:00:00Z"),
            ),
            AssistantMessage(
                id = "msg-2",
                role = MessageRole.ASSISTANT,
                markdown = "Next we review token refresh and sign-out.",
                createdAt = kotlinx.datetime.Instant.parse("2026-06-11T10:01:00Z"),
            ),
        )

        useCase("new topic: what changed in release notes?", conversationMessages = history)

        assertNotNull(engine.lastQuestion)
        assertTrue(engine.lastQuestion!!.startsWith("new topic: what changed in release notes?"))
        assertTrue("Conversation context:" !in engine.lastQuestion!!)
    }

    @Test
    fun createDocumentRequestBypassesEngineAndCreatesDraft() = runTest {
        val answer = useCase("Create a document about onboarding").getOrNull()

        assertNotNull(answer)
        assertEquals(AssistantIntentType.CREATE_DOCUMENT, answer.intent)
        assertEquals(0, engine.invocations)
        assertEquals(1, answer.sources.size)
        assertTrue(repository.stored.any { it.id == "created" })
    }
}
