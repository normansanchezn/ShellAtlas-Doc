package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDocumentRequestDto(
    val title: String,
    @SerialName("raw_markdown") val rawMarkdown: String,
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
)
