package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SourceStatus
import com.shelldocs.core.domain.entity.source.SyncLogEntry

/** Snapshot of the Imported Sources screen. */
data class SourcesState(
    val isLoading: Boolean = false,
    val sources: List<KnowledgeSource> = emptyList(),
    val syncLog: List<SyncLogEntry> = emptyList(),
    val syncingSourceIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
) : MviState {

    val totalImportedDocs: Int = sources.sumOf { it.importedDocs }

    val activeIntegrations: Int = sources.count { it.status == SourceStatus.CONNECTED }

    val lastSync = sources.mapNotNull { it.lastSyncAt }.maxOrNull()
}
