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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconChevronRight
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.molecules.ShellEmptyState
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsViewModel

private val EXPLORER_MIN_WIDTH = 180.dp
private val EXPLORER_MAX_WIDTH = 420.dp
private val EXPLORER_DEFAULT_WIDTH = 230.dp
private val ATTRIBUTES_MIN_WIDTH = 200.dp
private val ATTRIBUTES_MAX_WIDTH = 360.dp
private val ATTRIBUTES_DEFAULT_WIDTH = 240.dp

/**
 * Documents workspace. The reading pane always gets the remaining space;
 * the explorer tree and attributes rail start collapsed when a document is
 * open and can be expanded and resized by dragging their borders.
 */
@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors
    val density = LocalDensity.current

    var explorerWidth by remember { mutableStateOf(EXPLORER_DEFAULT_WIDTH) }
    var attributesWidth by remember { mutableStateOf(ATTRIBUTES_DEFAULT_WIDTH) }

    LaunchedEffect(viewModel) { viewModel.onIntent(DocumentsIntent.Initialize) }

    if (state.isEditing && state.selectedDocument != null) {
        DocumentEditorPanel(
            state = state,
            onIntent = viewModel::onIntent,
            isWide = isWide,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        Row(modifier = modifier.fillMaxSize().background(colors.background)) {
            if (isWide) {
                if (state.isExplorerExpanded) {
                    ExplorerTreePanel(
                        state = state,
                        onIntent = viewModel::onIntent,
                        modifier = Modifier.width(explorerWidth).fillMaxHeight(),
                    )
                    ResizeHandle(
                        onDrag = { deltaPx ->
                            explorerWidth = withDelta(explorerWidth, deltaPx, density, EXPLORER_MIN_WIDTH, EXPLORER_MAX_WIDTH)
                        },
                    )
                } else {
                    CollapsedPanelRail(
                        icon = IconChevronRight,
                        contentDescription = "Show explorer",
                        onClick = { viewModel.onIntent(DocumentsIntent.ToggleExplorerPanel) },
                        modifier = Modifier.fillMaxHeight(),
                    )
                }
            }
            DocumentListPanel(
                state = state,
                onIntent = viewModel::onIntent,
                modifier = Modifier.width(if (isWide) 250.dp else 230.dp).fillMaxHeight(),
            )
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val selected = state.selectedDocument
                if (selected == null) {
                    ShellEmptyState(
                        icon = IconFileText,
                        title = "Select a document to read",
                        subtitle = "or create a new document with + New",
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    DocumentReaderPanel(
                        state = state,
                        document = selected,
                        isWide = isWide,
                        onIntent = viewModel::onIntent,
                        attributesWidth = attributesWidth,
                        onResizeAttributes = { deltaPx ->
                            attributesWidth = withDelta(attributesWidth, -deltaPx, density, ATTRIBUTES_MIN_WIDTH, ATTRIBUTES_MAX_WIDTH)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    if (state.isAttributesDialogOpen) {
        AttributesEditDialog(state = state, onIntent = viewModel::onIntent)
    }
}

private fun withDelta(current: Dp, deltaPx: Float, density: Density, min: Dp, max: Dp): Dp {
    val deltaDp = with(density) { deltaPx.toDp() }
    return (current + deltaDp).coerceIn(min, max)
}
