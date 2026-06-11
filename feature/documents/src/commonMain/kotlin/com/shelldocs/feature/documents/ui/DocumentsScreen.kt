package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.molecules.ShellEmptyState
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsViewModel

/**
 * Documents workspace. Wide layouts show explorer tree + doc list + reader
 * (with attributes rail); compact layouts collapse to list -> reader.
 */
@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) { viewModel.onIntent(DocumentsIntent.Initialize) }

    Row(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (isWide) {
            ExplorerTreePanel(
                state = state,
                onIntent = viewModel::onIntent,
                modifier = Modifier.width(230.dp).fillMaxHeight(),
            )
        }
        DocumentListPanel(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.width(if (isWide) 250.dp else 230.dp).fillMaxHeight(),
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val selected = state.selectedDocument
            when {
                selected == null -> ShellEmptyState(
                    icon = IconFileText,
                    title = "Select a document to read",
                    subtitle = "or create a new document with + New",
                    modifier = Modifier.align(Alignment.Center),
                )
                state.isEditing -> DocumentEditorPanel(
                    state = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> DocumentReaderPanel(
                    state = state,
                    document = selected,
                    isWide = isWide,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
