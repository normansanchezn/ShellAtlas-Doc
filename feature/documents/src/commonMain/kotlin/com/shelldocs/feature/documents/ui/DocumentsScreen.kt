package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.molecules.ShellEmptyState
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.molecules.ShellScreenToolbar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.DocumentsStringRes
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsViewModel

private val EXPLORER_MIN_WIDTH = 180.dp
private val EXPLORER_MAX_WIDTH = 420.dp
private val EXPLORER_DEFAULT_WIDTH = 230.dp
private val ATTRIBUTES_MIN_WIDTH = 200.dp
private val ATTRIBUTES_MAX_WIDTH = 360.dp
private val ATTRIBUTES_DEFAULT_WIDTH = 240.dp
internal val COLLAPSED_RAIL_WIDTH = 32.dp

private enum class DocumentsMobilePane {
    Explorer,
    Reader,
}

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
    var mobilePane by remember { mutableStateOf(DocumentsMobilePane.Explorer) }

    LaunchedEffect(viewModel) { viewModel.onIntent(DocumentsIntent.Initialize) }
    LaunchedEffect(isWide, state.selectedDocument?.id) {
        if (isWide) return@LaunchedEffect
        mobilePane = if (state.selectedDocument == null) {
            DocumentsMobilePane.Explorer
        } else {
            DocumentsMobilePane.Reader
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (state.isCreatingDocument) {
            NewDocumentEditorPanel(
                state = state,
                onIntent = viewModel::onIntent,
                isWide = isWide,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (state.isEditing && state.selectedDocument != null) {
            DocumentEditorPanel(
                state = state,
                onIntent = viewModel::onIntent,
                isWide = isWide,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                ShellScreenToolbar(
                    title = DocumentsStringRes.TITLE,
                    subtitle = DocumentsStringRes.SUBTITLE,
                    modifier = Modifier.padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
                )
                if (isWide) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        ExplorerTreePanel(
                            state = state,
                            onIntent = viewModel::onIntent,
                            modifier = Modifier.width(explorerWidth).fillMaxHeight(),
                        )
                        ResizeHandle(
                            onDrag = { deltaPx ->
                                explorerWidth =
                                    withDelta(explorerWidth, deltaPx, density, EXPLORER_MIN_WIDTH, EXPLORER_MAX_WIDTH)
                            },
                        )
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    isWide = true,
                                    onIntent = viewModel::onIntent,
                                    attributesWidth = attributesWidth,
                                    onResizeAttributes = { deltaPx ->
                                        attributesWidth = withDelta(
                                            attributesWidth,
                                            -deltaPx,
                                            density,
                                            ATTRIBUTES_MIN_WIDTH,
                                            ATTRIBUTES_MAX_WIDTH
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MobilePaneSwitcher(
                            currentPane = mobilePane,
                            hasSelection = state.selectedDocument != null,
                            onExplorerClick = { mobilePane = DocumentsMobilePane.Explorer },
                            onReaderClick = {
                                if (state.selectedDocument != null) {
                                    mobilePane = DocumentsMobilePane.Reader
                                }
                            },
                        )
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when (mobilePane) {
                                DocumentsMobilePane.Explorer -> ExplorerTreePanel(
                                    state = state,
                                    onIntent = viewModel::onIntent,
                                    modifier = Modifier.fillMaxSize(),
                                )

                                DocumentsMobilePane.Reader -> {
                                    val selected = state.selectedDocument
                                    if (selected == null) {
                                        ShellEmptyState(
                                            icon = IconFileText,
                                            title = "Select a document to read",
                                            subtitle = "Switch back to Explorer to pick one.",
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    } else {
                                        DocumentReaderPanel(
                                            state = state,
                                            document = selected,
                                            isWide = false,
                                            onIntent = viewModel::onIntent,
                                            attributesWidth = attributesWidth,
                                            onResizeAttributes = { deltaPx ->
                                                attributesWidth = withDelta(
                                                    attributesWidth,
                                                    -deltaPx,
                                                    density,
                                                    ATTRIBUTES_MIN_WIDTH,
                                                    ATTRIBUTES_MAX_WIDTH
                                                )
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        state.loadingMessage?.let { message ->
            ShellLoadingOverlay(message = message)
        }
    }

    if (state.isAttributesDialogOpen) {
        AttributesEditDialog(state = state, onIntent = viewModel::onIntent)
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(DocumentsIntent.DismissError) },
        )
    }

    if (state.pendingDeleteDocumentId != null) {
        DeleteDocumentDialog(state = state, onIntent = viewModel::onIntent)
    }
}

private fun withDelta(current: Dp, deltaPx: Float, density: Density, min: Dp, max: Dp): Dp {
    val deltaDp = with(density) { deltaPx.toDp() }
    return (current + deltaDp).coerceIn(min, max)
}

@Composable
private fun MobilePaneSwitcher(
    currentPane: DocumentsMobilePane,
    hasSelection: Boolean,
    onExplorerClick: () -> Unit,
    onReaderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        WorkspaceModeButton(
            text = "Explorer",
            selected = currentPane == DocumentsMobilePane.Explorer,
            onClick = onExplorerClick,
            modifier = Modifier.weight(1f),
        )
        WorkspaceModeButton(
            text = "Reader",
            selected = currentPane == DocumentsMobilePane.Reader,
            enabled = hasSelection,
            onClick = onReaderClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WorkspaceModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ShellTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(if (selected) colors.surfaceSelected else colors.surfaceSubtle)
            .border(1.dp, if (selected) colors.brand.copy(alpha = 0.4f) else colors.border, RoundedCornerShape(ShellRadius.md))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = ShellSpacing.sm, horizontal = ShellSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ShellTheme.typography.label,
            color = if (selected) colors.textPrimary else colors.textSecondary.copy(alpha = if (enabled) 1f else 0.45f),
        )
    }
}
