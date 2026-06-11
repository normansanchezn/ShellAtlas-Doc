package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconChevronDown
import com.shelldocs.core.designsystem.icons.IconChevronUp
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.molecules.ShellProgressBar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.assistant.AnswerSource

/** Collapsible "Hide N sources" list with relevance meters. Clicking a source opens it in Documents. */
@Composable
fun SourcesList(
    sources: List<AnswerSource>,
    onSourceClick: (AnswerSource) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(ShellRadius.sm))
                .clickable { expanded = !expanded }
                .padding(vertical = ShellSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        ) {
            Icon(
                imageVector = IconFileText,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = if (expanded) "Hide ${sources.size} sources" else "Show ${sources.size} sources",
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
            )
            Icon(
                imageVector = if (expanded) IconChevronUp else IconChevronDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(11.dp),
            )
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                sources.forEachIndexed { index, source ->
                    SourceRow(rank = index + 1, source = source, onClick = { onSourceClick(source) })
                }
            }
        }
    }
}

@Composable
private fun SourceRow(rank: Int, source: AnswerSource, onClick: () -> Unit) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.surfaceSubtle)
            .clickable(onClick = onClick)
            .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(ShellRadius.sm))
                .background(colors.surfaceSelected),
            contentAlignment = Alignment.Center,
        ) {
            Text("$rank", style = ShellTheme.typography.caption, color = colors.brand)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(source.title, style = ShellTheme.typography.label, color = colors.textPrimary)
            if (source.breadcrumb.isNotBlank()) {
                Text(source.breadcrumb, style = ShellTheme.typography.caption, color = colors.textMuted)
            }
        }
        ShellProgressBar(
            progress = source.relevance / 100f,
            color = colors.success,
            modifier = Modifier.width(36.dp),
        )
        Text("${source.relevance}%", style = ShellTheme.typography.caption, color = colors.textMuted)
    }
}
