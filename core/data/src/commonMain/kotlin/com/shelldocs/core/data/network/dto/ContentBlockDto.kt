package com.shelldocs.core.data.network.dto

import kotlinx.serialization.Serializable

/** Wire form of one `content_json` block. */
@Serializable
data class ContentBlockDto(
    val type: String,
    val level: Int? = null,
    val text: String? = null,
    val style: String? = null,
    val items: List<String>? = null,
    val language: String? = null,
    val code: String? = null,
    val headers: List<String>? = null,
    val rows: List<List<String>>? = null,
)
