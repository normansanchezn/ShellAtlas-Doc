package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.atoms.ShellStatusBadge
import com.shelldocs.core.designsystem.icons.IconEdit
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.Document
import kotlin.time.ExperimentalTime

/** ATTRIBUTES rail: status, ownership, timeline and tags. */
@OptIn(ExperimentalTime::class)
@Composable
fun AttributesPanel(
    document: Document,
    modifier: Modifier = Modifier,
    canEdit: Boolean = false,
    onEdit: () -> Unit = {},
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier.padding(ShellSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShellSectionLabel(text = "Attributes", modifier = Modifier.weight(1f))
            if (canEdit) {
                ShellGhostButton(text = "Edit", icon = IconEdit, onClick = onEdit)
            }
        }

        AttributeGroupLabel("Status")
        ShellStatusBadge(status = document.status)

        AttributeGroupLabel("Ownership")
        AttributeRow("Owner", document.attributes.owner)
        AttributeRow("Module", document.attributes.module)
        AttributeRow("Team", document.attributes.team)
        AttributeRow("Platform", document.attributes.platform)

        AttributeGroupLabel("Timeline")
        AttributeRow("Created", document.createdAt.toString().substringBefore('T'))
        AttributeRow("Last updated", document.updatedAt.toString().substringBefore('T'))

        if (document.attributes.tags.isNotEmpty()) {
            AttributeGroupLabel("Tags")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
            ) {
                document.attributes.tags.forEach { tag ->
                    ShellBadge(
                        text = "#$tag",
                        contentColor = colors.textSecondary,
                        containerColor = colors.surfaceSubtle,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = ShellTheme.typography.sectionLabel,
        color = ShellTheme.colors.textMuted,
        modifier = Modifier.padding(top = ShellSpacing.sm),
    )
}

@Composable
private fun AttributeRow(label: String, value: String) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = ShellTheme.typography.caption, color = colors.textMuted)
        Text(
            text = value.ifBlank { "—" },
            style = ShellTheme.typography.caption,
            color = colors.textPrimary,
        )
    }
}
