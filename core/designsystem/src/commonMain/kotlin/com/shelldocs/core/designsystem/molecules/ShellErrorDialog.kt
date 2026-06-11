package com.shelldocs.core.designsystem.molecules

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.theme.ShellTheme

@Composable
fun ShellErrorDialog(
    state: ErrorDialogState,
    onDismiss: () -> Unit,
) {
    ShellDialog(
        title = state.title,
        onDismiss = onDismiss,
        actions = {
            ShellPrimaryButton(
                text = state.confirmLabel,
                onClick = onDismiss,
            )
        },
    ) {
        Text(
            text = state.message,
            style = ShellTheme.typography.body,
            color = ShellTheme.colors.textSecondary,
        )
    }
}
