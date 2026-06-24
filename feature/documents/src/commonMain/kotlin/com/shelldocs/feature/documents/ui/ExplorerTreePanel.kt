package com.shelldocs.feature.documents.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.icons.*
import com.shelldocs.core.designsystem.molecules.ShellContextMenu
import com.shelldocs.core.designsystem.molecules.ShellContextMenuItem
import com.shelldocs.core.designsystem.molecules.ShellSearchField
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.entity.document.DocumentNodeType
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/** EXPLORER rail: search, "+ New" and the folder tree. */
@Composable
fun ExplorerTreePanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier.background(colors.surface).padding(ShellSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        ) {
            ShellSectionLabel(text = "Explorer", modifier = Modifier.weight(1f))
            if (state.canEdit) {
                ShellIconButton(
                    icon = IconPlus,
                    contentDescription = "New document",
                    onClick = { onIntent(DocumentsIntent.StartCreatingDocument) },
                    modifier = Modifier.testTag(DemoTestTags.DocumentsNew),
                )
            }
        }
        ShellSearchField(
            value = state.filterQuery,
            onValueChange = { onIntent(DocumentsIntent.FilterChanged(it)) },
            placeholder = "Search...",
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (state.bookmarkedDocumentIds.isNotEmpty()) {
                BookmarksSection(state = state, onIntent = onIntent)
            }
            state.filteredTree?.let { root ->
                TreeNode(
                    node = root,
                    depth = 0,
                    state = state,
                    onIntent = onIntent,
                    forceExpanded = state.filterQuery.isNotBlank(),
                )
            }
        }
    }
}

@Composable
private fun BookmarksSection(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
) {
    val colors = ShellTheme.colors
    val bookmarkedDocs = state.documents.filter { it.id in state.bookmarkedDocumentIds }
    if (bookmarkedDocs.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = ShellSpacing.sm)) {
        ShellSectionLabel(
            text = "Bookmarks",
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
        )
        bookmarkedDocs.forEach { doc ->
            val isSelected = doc.id == state.selectedDocument?.id
            val background by animateColorAsState(
                targetValue = if (isSelected) colors.surfaceSelected else colors.surface,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "bookmarkNodeBackground",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ShellRadius.sm))
                    .background(background)
                    .clickable { onIntent(DocumentsIntent.SelectDocument(doc.id)) }
                    .padding(start = 6.dp, top = 5.dp, bottom = 5.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = IconBookmark,
                    contentDescription = null,
                    tint = if (isSelected) colors.brand else colors.textMuted,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = doc.title,
                    style = ShellTheme.typography.label,
                    color = if (isSelected) colors.brand else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TreeNode(
    node: DocumentNode,
    depth: Int,
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    forceExpanded: Boolean = false,
) {
    val colors = ShellTheme.colors
    val isExpanded = forceExpanded || node.id in state.expandedFolders
    val isSelected = node.documentId != null && node.documentId == state.selectedDocument?.id
    val background by animateColorAsState(
        targetValue = if (isSelected) colors.surfaceSelected else colors.surface,
        animationSpec = tween(ShellMotion.durationMedium),
        label = "treeNodeBackground",
    )
    var showContextMenu by remember(node.id) { mutableStateOf(false) }
    val canDelete = state.canDelete && node.type == DocumentNodeType.DOCUMENT && node.documentId != null

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ShellRadius.sm))
                .background(background)
                .combinedClickable(
                    onClick = {
                        when (node.type) {
                            DocumentNodeType.FOLDER -> onIntent(DocumentsIntent.ToggleFolder(node.id))
                            DocumentNodeType.DOCUMENT ->
                                node.documentId?.let { onIntent(DocumentsIntent.SelectDocument(it)) }
                        }
                    },
                    onLongClick = { if (canDelete) showContextMenu = true },
                )
                .padding(start = (depth * 14).dp + 6.dp, top = 5.dp, bottom = 5.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = when {
                    node.type == DocumentNodeType.DOCUMENT -> IconFileText
                    isExpanded -> IconChevronDown
                    else -> IconChevronRight
                },
                contentDescription = null,
                tint = if (isSelected) colors.brand else colors.textMuted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = node.title,
                style = ShellTheme.typography.label,
                color = when {
                    isSelected -> colors.brand
                    node.type == DocumentNodeType.FOLDER -> colors.textPrimary
                    else -> colors.textSecondary
                },
                maxLines = 1,
            )
        }

        if (canDelete) {
            ShellContextMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }) {
                ShellContextMenuItem(
                    text = "Delete",
                    icon = IconTrash,
                    tint = ShellTheme.colors.danger,
                    onClick = {
                        showContextMenu = false
                        node.documentId?.let {
                            onIntent(DocumentsIntent.RequestDeleteDocument(it, node.title))
                        }
                    },
                )
            }
        }
    }

    if (node.type == DocumentNodeType.FOLDER && isExpanded) {
        node.children.forEach { child ->
            TreeNode(
                node = child,
                depth = depth + 1,
                state = state,
                onIntent = onIntent,
                forceExpanded = forceExpanded,
            )
        }
    }
}
