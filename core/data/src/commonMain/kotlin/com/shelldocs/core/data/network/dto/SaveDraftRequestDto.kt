package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaveDraftRequestDto(
    @SerialName("raw_markdown") val rawMarkdown: String,
)
