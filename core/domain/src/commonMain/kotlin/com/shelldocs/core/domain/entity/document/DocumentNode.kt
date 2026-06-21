package com.shelldocs.core.domain.entity.document

/** Node of the explorer tree (folders derived from `parent_folder_id`). */
data class DocumentNode(
    val id: String,
    val title: String,
    val type: DocumentNodeType,
    val documentId: String? = null,
    val children: List<DocumentNode> = emptyList(),
) {
    fun flattenDocumentIds(): List<String> =
        listOfNotNull(documentId) + children.flatMap { it.flattenDocumentIds() }

    /** Prunes the tree down to document nodes whose id is in [matchingDocumentIds]; drops folders left empty. */
    fun pruneToDocuments(matchingDocumentIds: Set<String>): DocumentNode? = when (type) {
        DocumentNodeType.DOCUMENT -> takeIf { documentId in matchingDocumentIds }
        DocumentNodeType.FOLDER -> {
            val prunedChildren = children.mapNotNull { it.pruneToDocuments(matchingDocumentIds) }
            if (prunedChildren.isEmpty()) null else copy(children = prunedChildren)
        }
    }
}
