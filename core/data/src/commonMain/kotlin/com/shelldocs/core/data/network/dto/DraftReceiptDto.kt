package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DraftReceiptDto(
    @SerialName("document_id") val documentId: String,
    @SerialName("content_hash") val contentHash: String,
    @SerialName("updated_at") val updatedAt: String,
)
