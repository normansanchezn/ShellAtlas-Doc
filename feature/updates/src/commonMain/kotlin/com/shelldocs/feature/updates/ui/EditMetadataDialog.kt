package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellDropdown
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.ApplicationVersionCatalog
import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.entity.document.MetadataAttribute

/** Admin-only manual assignment for a missing/incorrect metadata attribute. */
@Composable
fun EditMetadataDialog(
    documentId: String,
    attribute: MetadataAttribute,
    currentVersion: String? = null,
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
                text = "Select a value for ${attribute.displayName.lowercase()}.",
                style = ShellTheme.typography.caption,
                color = ShellTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = ShellSpacing.sm),
            )
            when (attribute) {
                MetadataAttribute.AREA -> ShellDropdown(
                    selected = Area.entries.firstOrNull { it.displayName == value },
                    options = Area.entries,
                    label = { it.displayName },
                    onSelect = { value = it.displayName },
                    placeholder = "Select area",
                )

                MetadataAttribute.APPLICATION_VERSION -> ShellDropdown(
                    selected = value.ifBlank { null },
                    options = ApplicationVersionCatalog.selectableFrom(currentVersion),
                    label = { it },
                    onSelect = { value = it },
                    placeholder = "Select version",
                )

                else -> ShellTextField(
                    value = value,
                    onValueChange = { value = it },
                    placeholder = attribute.displayName,
                    onSubmit = { if (value.isNotBlank()) onConfirm(documentId, attribute, value) },
                )
            }
        }
    }
}
