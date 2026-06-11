package com.shelldocs.core.data.assistant

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequestDto(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
)
