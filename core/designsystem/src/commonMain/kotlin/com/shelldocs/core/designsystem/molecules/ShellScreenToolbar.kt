package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellElevation
import com.shelldocs.core.designsystem.tokens.ShellSpacing

private val ToolbarHeight = 64.dp

/**
 * Shared workspace toolbar for desktop/web screens. Material 3 top-app-bar layout: title/subtitle
 * start-aligned (never centered — a centered title with a long subtitle has nowhere to grow and
 * clips), leading content fixed-width only when present, trailing actions end-aligned.
 *
 * Every screen (Documents, Settings, Updates/Alerts, Dashboard, AI Assistant) must use this same
 * component for its top bar — do not hand-roll a per-screen header.
 */
@Composable
fun ShellScreenToolbar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .shadow(elevation = ShellElevation.raised)
            .background(colors.background)
            .padding(horizontal = ShellSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        if (leadingContent != null) {
            Row(verticalAlignment = Alignment.CenterVertically) { leadingContent() }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ShellTheme.typography.pageTitle,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailingContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
            ) {
                trailingContent()
            }
        }
    }
}
