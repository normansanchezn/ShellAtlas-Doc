package com.shelldocs.core.domain.entity.document

data class TableBlock(
    val headers: List<String>,
    val rows: List<List<String>>,
) : ContentBlock
