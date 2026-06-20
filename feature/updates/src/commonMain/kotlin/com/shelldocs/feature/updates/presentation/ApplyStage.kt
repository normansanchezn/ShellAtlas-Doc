package com.shelldocs.feature.updates.presentation

/** Sync pipeline run after Confirm on the final confirmation dialog. */
enum class ApplyStage(val message: String) {
    UPDATING_DATABASE("Updating Internal Database..."),
    SYNCING_CONFLUENCE("Updating Confluence..."),
    UPDATING_AZURE_DEVOPS("Updating Azure DevOps..."),
    UPDATING_SEARCH_INDEX("Updating Search Index..."),
    CREATING_VERSION_ENTRY("Creating Version Entry..."),
}
