package com.shelldocs.core.domain.entity.document

data class ListBlock(
    val style: ListStyle,
    val items: List<String>,
) : ContentBlock
