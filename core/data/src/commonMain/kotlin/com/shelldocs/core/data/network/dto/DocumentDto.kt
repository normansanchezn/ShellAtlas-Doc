package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire form of a document as returned by `/v1/documents`. */
@Serializable
data class DocumentDto(
    val id: String,
    val title: String,
    val summary: String = "",
    val status: String = "draft",
    val classification: String = "internal",
    @SerialName("raw_markdown") val rawMarkdown: String = "",
    @SerialName("content_json") val contentJson: ContentJsonDto = ContentJsonDto(),
    @SerialName("content_plaintext") val contentPlaintext: String = "",
    val attributes: DocumentAttributesDto = DocumentAttributesDto(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
