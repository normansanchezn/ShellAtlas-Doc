package com.shelldocs.core.data.repository

import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.entity.document.DocumentNodeType

/**
 * Pure function that groups documents into the explorer hierarchy:
 * root -> platform folders (from attributes) -> documents, mirroring the
 * "Shell App Documentation / iOS / Android / Cross-platform / Process" tree.
 */
object DocumentTreeBuilder {

    const val ROOT_TITLE = "Shell App Documentation"

    fun build(documents: List<Document>): DocumentNode {
        val byFolder = documents.groupBy { it.attributes.platform.ifBlank { "General" } }
        val folders = byFolder.entries
            .sortedBy { it.key.lowercase() }
            .map { (folder, docs) ->
                DocumentNode(
                    id = "folder-${folder.lowercase().replace(' ', '-')}",
                    title = folder,
                    type = DocumentNodeType.FOLDER,
                    children = docs.sortedBy { it.title.lowercase() }.map { document ->
                        DocumentNode(
                            id = "node-${document.id}",
                            title = document.title,
                            type = DocumentNodeType.DOCUMENT,
                            documentId = document.id,
                        )
                    },
                )
            }
        return DocumentNode(
            id = "root",
            title = ROOT_TITLE,
            type = DocumentNodeType.FOLDER,
            children = folders,
        )
    }
}
