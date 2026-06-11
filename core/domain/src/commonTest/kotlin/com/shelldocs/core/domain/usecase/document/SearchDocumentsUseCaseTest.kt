package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchDocumentsUseCaseTest {

    private val repository = FakeDocumentRepository(
        stored = listOf(
            DocumentFixtures.document(id = "auth", title = "Authentication"),
            DocumentFixtures.document(id = "push", title = "Push Notifications"),
        ),
    )
    private val search = SearchDocumentsUseCase(repository)

    @Test
    fun blankQueryShortCircuitsToEmptyList() = runTest {
        assertTrue(search("   ").getOrDefault(listOf(DocumentFixtures.document())).isEmpty())
    }

    @Test
    fun delegatesTrimmedQueryToRepository() = runTest {
        val results = search("  push ").getOrDefault(emptyList())
        assertEquals(listOf("push"), results.map { it.id })
    }
}
