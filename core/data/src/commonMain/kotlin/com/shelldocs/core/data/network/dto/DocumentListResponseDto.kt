package com.shelldocs.core.data.network.dto

import kotlinx.serialization.Serializable

/** Wire form of `GET /v1/documents` and `GET /v1/search` — both wrap the array. */
@Serializable
data class DocumentListResponseDto(
    val documents: List<DocumentDto> = emptyList(),
)
