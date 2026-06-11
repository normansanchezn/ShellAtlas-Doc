package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellColorScheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.domain.entity.document.DocumentStatus

/** Dot + label chip for document status (Published / Outdated / Draft ...). */
@Composable
fun ShellStatusBadge(status: DocumentStatus, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val (dot, container) = status.badgeColors(colors)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.full))
            .background(container)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(5.dp).clip(CircleShape).background(dot),
        )
        Text(text = status.displayName, style = ShellTheme.typography.caption, color = dot)
    }
}

private fun DocumentStatus.badgeColors(colors: ShellColorScheme): Pair<Color, Color> = when (this) {
    DocumentStatus.PUBLISHED -> colors.success to colors.successSoft
    DocumentStatus.OUTDATED -> colors.warning to colors.warningSoft
    DocumentStatus.UPDATES_PENDING -> colors.brand to colors.surfaceSelected
    DocumentStatus.CONFLICTED, DocumentStatus.DELETED_SOURCE -> colors.danger to colors.dangerSoft
    DocumentStatus.DRAFT, DocumentStatus.ARCHIVED, DocumentStatus.LOCKED ->
        colors.textMuted to colors.surfaceSubtle
}
