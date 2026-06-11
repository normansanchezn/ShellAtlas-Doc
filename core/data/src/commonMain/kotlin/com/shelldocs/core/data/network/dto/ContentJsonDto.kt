package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentJsonDto(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    val blocks: List<ContentBlockDto> = emptyList(),
)
