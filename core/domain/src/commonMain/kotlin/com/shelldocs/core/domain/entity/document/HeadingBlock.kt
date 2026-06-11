package com.shelldocs.core.domain.entity.document

data class HeadingBlock(
    val level: Int,
    val text: String,
) : ContentBlock
