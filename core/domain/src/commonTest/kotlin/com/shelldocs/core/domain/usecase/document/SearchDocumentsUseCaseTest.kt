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
            DocumentFixtures.document(
                id = "eosb1",
                title = "EoSB1 Process for America's App - Android",
                tags = listOf("release", "build"),
                platform = "Android",
            ),
            DocumentFixtures.document(
                id = "lokalise",
                title = "Lokalise Strings Update Process",
                tags = listOf("lokalise", "translations"),
                platform = "Cross-platform",
            ),
            DocumentFixtures.document(id = "push", title = "Push Notifications"),
        ),
    )
    private val search = SearchDocumentsUseCase(repository)

    @Test
    fun blankQueryShortCircuitsToEmptyList() = runTest {
        assertTrue(search("   ").getOrDefault(listOf(DocumentFixtures.document())).isEmpty())
    }

    @Test
    fun releaseBuildAliasFindsEosb1Document() = runTest {
        val results = search(" release build ").getOrDefault(emptyList())

        assertEquals("eosb1", results.firstOrNull()?.id)
    }

    @Test
    fun localizationAliasFindsLokaliseDocument() = runTest {
        val results = search("localization").getOrDefault(emptyList())

        assertEquals("lokalise", results.firstOrNull()?.id)
    }
}
