package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellColorScheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataSuggestion
import com.shelldocs.feature.updates.UpdatesStringRes

/** Document / Missing Attributes / Confidence / Source / Suggested Values / Actions. */
@Composable
fun MetadataIssuesTable(
    issues: List<DocumentClassificationResult>,
    isAdmin: Boolean,
    isWide: Boolean,
    onEditMetadata: (DocumentClassificationResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSubtle)
                    .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderCell(UpdatesStringRes.HEADER_DOCUMENT, Modifier.weight(2.2f))
                HeaderCell(UpdatesStringRes.HEADER_MISSING, Modifier.weight(1.6f))
                if (isWide) HeaderCell(UpdatesStringRes.HEADER_SOURCE, Modifier.weight(1f))
                HeaderCell(UpdatesStringRes.HEADER_AI_SUGGESTION, Modifier.weight(2.2f))
                if (isAdmin) HeaderCell(UpdatesStringRes.HEADER_ACTIONS, Modifier.width(120.dp))
            }
            issues.forEach { issue ->
                MetadataIssueRow(issue = issue, isAdmin = isAdmin, isWide = isWide, onEditMetadata = onEditMetadata)
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = ShellTheme.typography.sectionLabel,
        color = ShellTheme.colors.textMuted,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun MetadataIssueRow(
    issue: DocumentClassificationResult,
    isAdmin: Boolean,
    isWide: Boolean,
    onEditMetadata: (DocumentClassificationResult) -> Unit,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(2.2f)) {
            Text(issue.documentTitle, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
        }
        Column(modifier = Modifier.weight(1.6f), verticalArrangement = Arrangement.spacedBy(ShellSpacing.xxs)) {
            issue.missingAttributes.forEach { attribute ->
                Text(attribute.displayName, style = ShellTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (isWide) {
            Text(
                text = issue.sourceType.displayName,
                style = ShellTheme.typography.label,
                color = colors.textMuted,
                modifier = Modifier.weight(1f),
            )
        }
        Column(modifier = Modifier.weight(2.2f), verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
            issue.suggestions.forEach { suggestion -> SuggestionLine(suggestion) }
            if (issue.suggestions.isEmpty()) {
                Text(UpdatesStringRes.NO_AI_SUGGESTION, style = ShellTheme.typography.caption, color = colors.textMuted)
            }
        }
        if (isAdmin) {
            Box(modifier = Modifier.width(120.dp)) {
                ShellPrimaryButton(
                    text = UpdatesStringRes.EDIT,
                    onClick = { onEditMetadata(issue) },
                )
            }
        }
    }
}

@Composable
private fun SuggestionLine(suggestion: MetadataSuggestion) {
    val colors = ShellTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        Text(
            text = "${suggestion.attribute.displayName}: ${suggestion.suggestedValue}",
            style = ShellTheme.typography.caption,
            color = colors.textPrimary,
        )
        ShellBadge(
            text = "${suggestion.confidencePercent}%",
            contentColor = confidenceColor(suggestion.confidencePercent, colors),
            containerColor = colors.surfaceSubtle,
        )
    }
}

@Composable
private fun confidenceColor(confidencePercent: Int, colors: ShellColorScheme) = when {
    confidencePercent >= 80 -> colors.success
    confidencePercent >= 50 -> colors.warning
    else -> colors.danger
}
