package com.shelldocs.feature.updates.presentation

/** Pre-analysis pipeline run before the AI Suggested Update screen shows any content. */
enum class AnalysisStage(val message: String) {
    ANALYZING_DOCUMENT("Analyzing document..."),
    RETRIEVING_RELATED_DOCUMENTS("Retrieving related documents..."),
    RETRIEVING_MATCHING_VERSIONS("Retrieving matching versions..."),
    RETRIEVING_CONFLUENCE_CONTENT("Retrieving related Confluence content..."),
    RETRIEVING_AZURE_DEVOPS_REFERENCES("Retrieving Azure DevOps references..."),
    GENERATING_SUGGESTED_CHANGES("Generating suggested changes..."),
}
