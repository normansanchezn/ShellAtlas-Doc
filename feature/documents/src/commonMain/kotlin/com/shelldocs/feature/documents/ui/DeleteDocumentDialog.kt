package com.shelldocs.feature.documents.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/** Confirms an irreversible delete: removes the page from Confluence and soft-deletes it in the DB. */
@Composable
fun DeleteDocumentDialog(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
) {
    ShellDialog(
        title = "Delete \"${state.pendingDeleteDocumentTitle}\"?",
        onDismiss = { onIntent(DocumentsIntent.CancelDeleteDocument) },
        actions = {
            ShellGhostButton(
                text = "Cancel",
                onClick = { onIntent(DocumentsIntent.CancelDeleteDocument) },
                enabled = !state.isDeletingDocument,
            )
            ShellPrimaryButton(
                text = if (state.isDeletingDocument) "Deleting..." else "Delete",
                onClick = { onIntent(DocumentsIntent.ConfirmDeleteDocument) },
                enabled = !state.isDeletingDocument,
            )
        },
    ) {
        Text(
            text = "This removes the document from Confluence and from the database. This cannot be undone.",
            style = ShellTheme.typography.body,
            color = ShellTheme.colors.textSecondary,
        )
    }
}
