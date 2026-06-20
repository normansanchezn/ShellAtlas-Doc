package com.shelldocs.feature.updates.presentation

/** Independent loading states for the Apply Update pipeline (per spec's States section). */
enum class ApplyStage(val message: String) {
    SAVING_DOCUMENT("Saving Document..."),
    UPDATING_METADATA("Updating Metadata..."),
    SYNCING_CONFLUENCE("Syncing Confluence..."),
    UPDATING_AZURE_DEVOPS("Updating Azure DevOps..."),
    REINDEXING_SEARCH("Reindexing Search..."),
}
