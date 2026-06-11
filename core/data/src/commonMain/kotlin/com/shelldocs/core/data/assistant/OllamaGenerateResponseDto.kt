package com.shelldocs.core.data.assistant

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateResponseDto(
    val response: String = "",
)
