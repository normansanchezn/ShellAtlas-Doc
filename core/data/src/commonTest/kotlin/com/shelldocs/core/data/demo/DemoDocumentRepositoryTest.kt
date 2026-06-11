package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.document.DocumentStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoDocumentRepositoryTest {

    private val repository = DemoDocumentRepository(timeProvider = TimeProvider { DemoSeed.now })

    @Test
    fun seedDocumentsAreListedAlphabetically() = runTest {
        val titles = repository.documents().getOrDefault(emptyList()).map { it.title }
        assertEquals(titles.sortedBy { it.lowercase() }, titles)
        assertEquals(DemoSeed.documents.size, titles.size)
    }

    @Test
    fun searchMatchesTitleBodyAndTags() = runTest {
        val byTitle = repository.search("loyalty").getOrDefault(emptyList())
        val byBody = repository.search("PKCE").getOrDefault(emptyList())
        val byTag = repository.search("apns").getOrDefault(emptyList())

        assertTrue(byTitle.any { it.id == "doc-loyalty" })
        assertTrue(byBody.any { it.id == "doc-authentication" })
        assertTrue(byTag.any { it.id == "doc-push-notifications" })
    }

    @Test
    fun publishCreatesNewVersionAndMarksPublished() = runTest {
        val updated = repository.publish("doc-android-auth", "# Android Auth\n\nFinal version.", "Completed rollout").getOrNull()

        assertNotNull(updated)
        assertEquals(DocumentStatus.PUBLISHED, updated.status)
        val versions = repository.versions("doc-android-auth").getOrDefault(emptyList())
        assertEquals(2, versions.size)
        assertEquals("Completed rollout", versions.first().changeSummary)
    }

    @Test
    fun restoreVersionRepublishesOldContent() = runTest {
        val original = repository.document("doc-station-locator").getOrNull()!!.rawMarkdown
        repository.publish("doc-station-locator", "# Station Locator\n\nRewritten.", "Rewrite")

        val versions = repository.versions("doc-station-locator").getOrDefault(emptyList())
        val firstVersion = versions.last()
        val restored = repository.restoreVersion("doc-station-locator", firstVersion.id).getOrNull()

        assertNotNull(restored)
        assertEquals(original, restored.rawMarkdown)
        assertEquals(3, repository.versions("doc-station-locator").getOrDefault(emptyList()).size)
    }

    @Test
    fun createAddsDraftDocument() = runTest {
        val created = repository.create("Offline Mode", "# Offline Mode\n\nDraft.", parentFolderId = null).getOrNull()

        assertNotNull(created)
        assertEquals(DocumentStatus.DRAFT, created.status)
        assertTrue(repository.documents().getOrDefault(emptyList()).any { it.id == created.id })
    }

    @Test
    fun deleteRemovesDocumentAndHistory() = runTest {
        repository.delete("doc-loyalty")

        assertIs<DomainResult.Failure>(repository.document("doc-loyalty"))
        assertIs<DomainResult.Failure>(repository.versions("doc-loyalty"))
    }

    @Test
    fun draftReceiptCarriesContentHash() = runTest {
        val receipt = repository.saveDraft("doc-authentication", "# Draft body").getOrNull()

        assertNotNull(receipt)
        assertEquals("doc-authentication", receipt.documentId)
        assertTrue(receipt.contentHash.isNotBlank())
    }
}
