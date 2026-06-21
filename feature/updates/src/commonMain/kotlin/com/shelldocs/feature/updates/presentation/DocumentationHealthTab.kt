package com.shelldocs.feature.updates.presentation

import com.shelldocs.feature.updates.UpdatesStringRes

/** Sections within the Documentation Health screen. */
enum class DocumentationHealthTab(val label: String) {
    HEALTH(UpdatesStringRes.TAB_HEALTH),
    METADATA_ISSUES(UpdatesStringRes.TAB_METADATA_ISSUES),
    HEALTHY(UpdatesStringRes.TAB_HEALTHY),
}
