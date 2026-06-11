package com.shelldocs.core.domain.entity.document

data class CodeBlock(
    val language: String,
    val code: String,
) : ContentBlock
