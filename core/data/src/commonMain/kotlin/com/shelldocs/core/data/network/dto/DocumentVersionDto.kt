package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentVersionDto(
    val id: String,
    @SerialName("document_id") val documentId: String,
    @SerialName("version_number") val versionNumber: Int,
    val title: String = "",
    @SerialName("raw_markdown") val rawMarkdown: String = "",
    @SerialName("change_summary") val changeSummary: String = "",
    @SerialName("created_at") val createdAt: String,
)
