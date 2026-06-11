package com.shelldocs.core.domain.entity.document

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentNodeTest {

    @Test
    fun flattenCollectsDocumentIdsDepthFirst() {
        val tree = DocumentNode(
            id = "root",
            title = "Shell App Documentation",
            type = DocumentNodeType.FOLDER,
            children = listOf(
                DocumentNode(
                    id = "ios",
                    title = "iOS",
                    type = DocumentNodeType.FOLDER,
                    children = listOf(
                        DocumentNode("n1", "Authentication", DocumentNodeType.DOCUMENT, documentId = "doc-auth"),
                    ),
                ),
                DocumentNode("n2", "Release Process", DocumentNodeType.DOCUMENT, documentId = "doc-release"),
            ),
        )

        assertEquals(listOf("doc-auth", "doc-release"), tree.flattenDocumentIds())
    }
}
