package com.shelldocs.core.data.repository

import com.shelldocs.core.data.demo.DemoSeed
import com.shelldocs.core.domain.entity.document.DocumentNodeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentTreeBuilderTest {

    @Test
    fun groupsDocumentsIntoPlatformFolders() {
        val tree = DocumentTreeBuilder.build(DemoSeed.documents)

        assertEquals(DocumentTreeBuilder.ROOT_TITLE, tree.title)
        assertEquals(DocumentNodeType.FOLDER, tree.type)
        val folderTitles = tree.children.map { it.title }
        assertEquals(listOf("Android", "Cross-platform", "Process", "iOS"), folderTitles)
    }

    @Test
    fun everySeedDocumentIsReachableExactlyOnce() {
        val tree = DocumentTreeBuilder.build(DemoSeed.documents)

        val ids = tree.flattenDocumentIds()
        assertEquals(DemoSeed.documents.size, ids.size)
        assertEquals(ids.toSet().size, ids.size)
    }

    @Test
    fun documentsAreSortedAlphabeticallyInsideFolders() {
        val tree = DocumentTreeBuilder.build(DemoSeed.documents)
        val ios = tree.children.first { it.title == "iOS" }

        val titles = ios.children.map { it.title }
        assertEquals(titles.sortedBy { it.lowercase() }, titles)
        assertTrue(ios.children.all { it.type == DocumentNodeType.DOCUMENT })
    }
}
