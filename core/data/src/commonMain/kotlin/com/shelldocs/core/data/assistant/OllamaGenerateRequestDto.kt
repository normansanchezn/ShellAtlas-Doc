package com.shelldocs.core.data.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequestDto(
    val model: String,
    val prompt: String,
    val stream: Boolean,
    val options: OllamaOptionsDto,
)

@Serializable
data class OllamaOptionsDto(
    @SerialName("num_ctx") val numCtx: Int,
)
