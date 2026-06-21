package com.shelldocs.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentAttributesDto(
    val owner: String = "",
    val module: String = "",
    val team: String = "",
    val platform: String = "",
    @SerialName("parent_folder_id") val parentFolderId: String? = null,
    val tags: List<String> = emptyList(),
    val area: String = "",
    @SerialName("application_version") val applicationVersion: String = "",
)
