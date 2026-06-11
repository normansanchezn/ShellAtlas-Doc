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
}
