package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/** Popup form for editing a document's owner, module, team, platform and tags. */
@Composable
fun AttributesEditDialog(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
) {
    val draft = state.attributesDraft
    val colors = ShellTheme.colors
    ShellDialog(
        title = "Edit attributes",
        onDismiss = { onIntent(DocumentsIntent.CloseAttributesEditor) },
        actions = {
            ShellGhostButton(text = "Cancel", onClick = { onIntent(DocumentsIntent.CloseAttributesEditor) })
            ShellPrimaryButton(text = "Save", onClick = { onIntent(DocumentsIntent.SaveAttributes) })
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm)) {
            AttributeField(
                label = "Owner",
                value = draft.owner,
                onValueChange = { onIntent(DocumentsIntent.AttributesOwnerChanged(it)) },
            )
            AttributeField(
                label = "Module",
                value = draft.module,
                onValueChange = { onIntent(DocumentsIntent.AttributesModuleChanged(it)) },
            )
            AttributeField(
                label = "Team",
                value = draft.team,
                onValueChange = { onIntent(DocumentsIntent.AttributesTeamChanged(it)) },
            )
            AttributeField(
                label = "Platform",
                value = draft.platform,
                onValueChange = { onIntent(DocumentsIntent.AttributesPlatformChanged(it)) },
            )
            AttributeField(
                label = "Tags (comma-separated)",
                value = draft.tagsText,
                onValueChange = { onIntent(DocumentsIntent.AttributesTagsChanged(it)) },
            )
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = ShellTheme.typography.caption,
                    color = colors.danger,
                )
            }
        }
    }
}

@Composable
private fun AttributeField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
        Text(text = label, style = ShellTheme.typography.caption, color = ShellTheme.colors.textMuted)
        ShellTextField(value = value, onValueChange = onValueChange, modifier = Modifier)
    }
}
