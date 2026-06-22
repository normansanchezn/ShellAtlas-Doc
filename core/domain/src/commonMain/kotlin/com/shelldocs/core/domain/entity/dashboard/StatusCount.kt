package com.shelldocs.core.domain.entity.dashboard

import com.shelldocs.core.domain.entity.document.DocumentStatus

/** How many documents currently sit in [status], and what share of the corpus that is. */
data class StatusCount(
    val status: DocumentStatus,
    val count: Int,
    val percent: Int,
)
