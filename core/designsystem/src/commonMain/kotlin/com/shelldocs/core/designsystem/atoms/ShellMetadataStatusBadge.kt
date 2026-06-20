package com.shelldocs.core.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.domain.entity.document.MetadataClassificationStatus

/** Uppercase chip for a document's metadata classification outcome. */
@Composable
fun ShellMetadataStatusBadge(status: MetadataClassificationStatus, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val (content, container) = when (status) {
        MetadataClassificationStatus.REQUIRES_ATTENTION -> colors.danger to colors.dangerSoft
        MetadataClassificationStatus.NEEDS_REVIEW -> colors.warning to colors.warningSoft
        MetadataClassificationStatus.READY -> colors.success to colors.successSoft
    }
    ShellBadge(
        text = status.displayName.uppercase(),
        contentColor = content,
        containerColor = container,
        modifier = modifier,
    )
}
