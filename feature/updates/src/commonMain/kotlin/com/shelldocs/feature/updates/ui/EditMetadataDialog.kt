package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.MetadataAttribute

/** Admin-only manual assignment for a missing/incorrect metadata attribute. */
@Composable
fun EditMetadataDialog(
    documentId: String,
    attribute: MetadataAttribute,
    onDismiss: () -> Unit,
    onConfirm: (documentId: String, attribute: MetadataAttribute, value: String) -> Unit,
) {
    var value by remember(documentId, attribute) { mutableStateOf("") }
    ShellDialog(
        title = "Assign ${attribute.displayName}",
        onDismiss = onDismiss,
        actions = {
            ShellGhostButton(text = "Cancel", onClick = onDismiss)
            ShellPrimaryButton(
                text = "Save",
                onClick = { onConfirm(documentId, attribute, value) },
                enabled = value.isNotBlank(),
            )
        },
    ) {
        Column {
            Text(
                text = "Enter a value for ${attribute.displayName.lowercase()}.",
                style = ShellTheme.typography.caption,
                color = ShellTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = ShellSpacing.sm),
            )
            ShellTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = attribute.displayName,
                onSubmit = { if (value.isNotBlank()) onConfirm(documentId, attribute, value) },
            )
        }
    }
}
