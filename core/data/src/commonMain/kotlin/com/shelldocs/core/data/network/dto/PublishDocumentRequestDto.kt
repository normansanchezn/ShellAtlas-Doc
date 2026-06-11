package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublishDocumentRequestDto(
    @SerialName("raw_markdown") val rawMarkdown: String,
    @SerialName("change_summary") val changeSummary: String,
)
