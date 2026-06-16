package com.shelldocs.feature.documents.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellStatusBadge
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.icons.IconBookmark
import com.shelldocs.core.designsystem.icons.IconEdit
import com.shelldocs.core.designsystem.icons.IconHistory
import com.shelldocs.core.designsystem.icons.IconShare
import com.shelldocs.core.designsystem.icons.IconLayers
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState
import kotlin.time.ExperimentalTime

/** Reader: breadcrumb bar, rendered Markdown body and the attributes rail. */
@OptIn(ExperimentalTime::class)
@Composable
fun DocumentReaderPanel(
    state: DocumentsState,
    document: Document,
    isWide: Boolean,
    onIntent: (DocumentsIntent) -> Unit,
    attributesWidth: Dp,
    onResizeAttributes: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        ) {
            DocumentBreadcrumb(
                document = document,
                onNavigate = { folderId -> onIntent(DocumentsIntent.BreadcrumbNavigate(folderId)) },
                modifier = Modifier.weight(1f),
            )
            ShellGhostButton(
                text = "History",
                icon = IconHistory,
                onClick = { onIntent(DocumentsIntent.ShowHistory) },
                enabled = !state.isBusy,
                modifier = Modifier.testTag(DemoTestTags.DocumentsHistory),
            )
            if (!isWide) {
                ShellGhostButton(
                    text = if (state.isAttributesExpanded) "Hide attributes" else "Attributes",
                    icon = IconLayers,
                    onClick = { onIntent(DocumentsIntent.ToggleAttributesPanel) },
                    enabled = !state.isBusy,
                )
            }
            if (state.canEdit) {
                ShellPrimaryButton(
                    text = "Edit",
                    icon = IconEdit,
                    onClick = { onIntent(DocumentsIntent.StartEditing) },
                    enabled = !state.isBusy,
                    modifier = Modifier.testTag(DemoTestTags.DocumentsEdit),
                )
            }
        }
        if (isWide) {
            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ShellSpacing.xxxl, vertical = ShellSpacing.xl),
                ) {
                    documentBody(document = document, state = state, colors = colors, onIntent = onIntent)
                }
                if (state.isHistoryVisible) {
                    ResizeHandle(onDrag = onResizeAttributes)
                    VersionHistoryPanel(
                        state = state,
                        onIntent = onIntent,
                        modifier = Modifier.width(attributesWidth).fillMaxHeight(),
                    )
                } else {
                    val attributesPanelWidth by animateDpAsState(
                        targetValue = if (state.isAttributesExpanded) attributesWidth else COLLAPSED_RAIL_WIDTH,
                        animationSpec = tween(ShellMotion.durationMedium, easing = ShellMotion.standard),
                        label = "attributesPanelWidth",
                    )
                    if (state.isAttributesExpanded) {
                        ResizeHandle(onDrag = onResizeAttributes)
                    }
                    Box(
                        modifier = Modifier
                            .width(attributesPanelWidth)
                            .fillMaxHeight()
                            .clipToBounds(),
                    ) {
                        if (state.isAttributesExpanded) {
                            AttributesPanel(
                                document = document,
                                modifier = Modifier.width(attributesWidth).fillMaxHeight(),
                                canEdit = state.canEdit,
                                onEdit = { onIntent(DocumentsIntent.OpenAttributesEditor) },
                                onCollapse = { onIntent(DocumentsIntent.ToggleAttributesPanel) },
                            )
                        } else {
                            CollapsedPanelRail(
                                icon = IconLayers,
                                contentDescription = "Show attributes",
                                onClick = { onIntent(DocumentsIntent.ToggleAttributesPanel) },
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
            ) {
                documentBody(document = document, state = state, colors = colors, onIntent = onIntent)
                when {
                    state.isHistoryVisible -> VersionHistoryPanel(
                        state = state,
                        onIntent = onIntent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.isAttributesExpanded -> AttributesPanel(
                        document = document,
                        modifier = Modifier.fillMaxWidth(),
                        canEdit = state.canEdit,
                        onEdit = { onIntent(DocumentsIntent.OpenAttributesEditor) },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTime::class)
private fun documentBody(
    document: Document,
    state: DocumentsState,
    colors: com.shelldocs.core.designsystem.tokens.ShellColorScheme,
    onIntent: (DocumentsIntent) -> Unit = {},
) {
    ShellStatusBadge(status = document.status)
    Text(
        text = document.title,
        style = ShellTheme.typography.displayTitle,
        color = colors.textPrimary,
        modifier = Modifier.padding(top = ShellSpacing.md),
    )
    Text(
        text = document.summary,
        style = ShellTheme.typography.body,
        color = colors.textSecondary,
        modifier = Modifier.padding(top = ShellSpacing.xs),
    )
    Text(
        text = "${document.attributes.owner}  ·  Updated ${document.updatedAt.toString().substringBefore('T')}",
        style = ShellTheme.typography.caption,
        color = colors.textMuted,
        modifier = Modifier.padding(top = ShellSpacing.md, bottom = ShellSpacing.lg),
    )
    MarkdownBlocksView(
        blocks = document.content.blocks,
        modifier = Modifier.widthIn(max = 680.dp),
    )
    Row(
        modifier = Modifier.padding(top = ShellSpacing.xl),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        ShellGhostButton(
            text = "Share",
            icon = IconShare,
            onClick = { onIntent(com.shelldocs.feature.documents.presentation.DocumentsIntent.ExportPdf) },
            enabled = !state.isBusy,
        )
        val isBookmarked = document.id in state.bookmarkedDocumentIds
        ShellGhostButton(
            text = if (isBookmarked) "Bookmarked" else "Bookmark",
            icon = IconBookmark,
            onClick = { onIntent(com.shelldocs.feature.documents.presentation.DocumentsIntent.ToggleBookmark(document.id)) },
            enabled = !state.isBusy,
            modifier = Modifier.testTag(DemoTestTags.DocumentsBookmark),
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DocumentBreadcrumb(
    document: Document,
    onNavigate: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val segments = buildList {
        add(BreadcrumbSegment("Docs", null))
        val platform = document.attributes.platform
        if (platform.isNotBlank()) {
            add(BreadcrumbSegment(platform, "folder-$platform"))
        }
        val module = document.attributes.module
        if (module.isNotBlank() && module != platform) {
            add(BreadcrumbSegment(module, "folder-$module"))
        }
        add(BreadcrumbSegment(document.title, null))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        segments.forEachIndexed { index, segment ->
            val isLast = index == segments.lastIndex
            Text(
                text = segment.label,
                style = ShellTheme.typography.caption,
                color = if (isLast) colors.textPrimary else colors.brand,
                maxLines = 1,
                modifier = if (!isLast) {
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onNavigate(segment.folderId) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                },
            )
            if (!isLast) {
                Text(
                    text = "›",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}

private data class BreadcrumbSegment(val label: String, val folderId: String?)
