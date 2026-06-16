package com.shelldocs.core.domain.entity.assistant

import com.shelldocs.core.domain.entity.document.Document

/** Retrieval result: a candidate grounding document and its relevance (0..1+). */
data class ScoredDocument(
    val document: Document,
    val score: Double,
) {
    val relevancePercent: Int = (score * 100).toInt().coerceIn(1, 99)
    val isHighConfidence: Boolean get() = score >= 0.62
    val isPartialMatch: Boolean get() = score in 0.01..0.31
}
