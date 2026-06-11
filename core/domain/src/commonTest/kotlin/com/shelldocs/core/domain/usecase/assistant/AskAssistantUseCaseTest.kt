package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import com.shelldocs.core.domain.repository.AssistantCacheRepository
import com.shelldocs.core.domain.repository.AssistantEngine
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class RecordingEngine : AssistantEngine {
    var invocations = 0
    var lastIntent: AssistantIntentType? = null
    var lastGrounding: List<ScoredDocument> = emptyList()

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        groundingDocuments: List<ScoredDocument>,
    ): DomainResult<AssistantAnswer> {
        invocations++
        lastIntent = intent
        lastGrounding = groundingDocuments
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
    fun createDocumentRequestBypassesEngineAndCreatesDraft() = runTest {
        val answer = useCase("Create a document about onboarding").getOrNull()

        assertNotNull(answer)
        assertEquals(AssistantIntentType.CREATE_DOCUMENT, answer.intent)
        assertEquals(0, engine.invocations)
        assertEquals(1, answer.sources.size)
        assertTrue(repository.stored.any { it.id == "created" })
    }
}
