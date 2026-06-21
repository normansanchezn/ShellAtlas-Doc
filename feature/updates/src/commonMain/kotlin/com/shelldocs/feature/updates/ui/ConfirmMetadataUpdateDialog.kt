package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataAssignment
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.feature.updates.UpdatesStringRes

@Composable
fun ConfirmMetadataUpdateDialog(
    issue: DocumentClassificationResult,
    assignments: List<MetadataAssignment>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ShellDialog(
        title = UpdatesStringRes.METADATA_CONFIRM_TITLE,
        onDismiss = onDismiss,
        actions = {
            ShellGhostButton(text = UpdatesStringRes.CANCEL, onClick = onDismiss)
            ShellPrimaryButton(text = UpdatesStringRes.CONFIRM, onClick = onConfirm)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            Text(
                text = issue.documentTitle,
                style = ShellTheme.typography.bodyStrong,
                color = ShellTheme.colors.textPrimary,
            )
            Text(
                text = UpdatesStringRes.METADATA_CONFIRM_DESCRIPTION,
                style = ShellTheme.typography.caption,
                color = ShellTheme.colors.textMuted,
            )
            Text(
                text = UpdatesStringRes.METADATA_CONFIRM_CHANGES,
                style = ShellTheme.typography.caption,
                color = ShellTheme.colors.textMuted,
            )
            assignments.forEach { assignment ->
                Text(
                    text = "${attributeLabel(assignment.attribute)}: ${assignment.value}",
                    style = ShellTheme.typography.body,
                    color = ShellTheme.colors.textPrimary,
                )
            }
        }
    }
}

private fun attributeLabel(attribute: MetadataAttribute): String = when (attribute) {
    MetadataAttribute.OWNER -> UpdatesStringRes.FIELD_OWNER
    MetadataAttribute.AREA -> UpdatesStringRes.FIELD_AREA
    MetadataAttribute.APPLICATION_VERSION -> UpdatesStringRes.FIELD_APPLICATION_VERSION
    MetadataAttribute.PLATFORM -> UpdatesStringRes.FIELD_PLATFORM
    MetadataAttribute.TAGS -> UpdatesStringRes.FIELD_TAGS
    MetadataAttribute.TEAM -> UpdatesStringRes.FIELD_TEAM
    MetadataAttribute.MODULE -> UpdatesStringRes.FIELD_MODULE
    MetadataAttribute.DOCUMENT_TYPE -> attribute.displayName
}
