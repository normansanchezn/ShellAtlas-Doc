package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.updates.presentation.DocumentationHealthTab

/** Segmented switcher between the Health table and Metadata Issues sections. */
@Composable
fun DocumentationHealthTabRow(
    selectedTab: DocumentationHealthTab,
    metadataIssueCount: Int,
    onSelect: (DocumentationHealthTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.surfaceSubtle)
            .padding(ShellSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xxs),
    ) {
        DocumentationHealthTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val label = if (tab == DocumentationHealthTab.METADATA_ISSUES && metadataIssueCount > 0) {
                "${tab.label} (${metadataIssueCount})"
            } else {
                tab.label
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(ShellRadius.sm))
                    .background(if (isSelected) colors.brand else colors.surfaceSubtle)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = ShellTheme.typography.label,
                    color = if (isSelected) colors.onBrand else colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
