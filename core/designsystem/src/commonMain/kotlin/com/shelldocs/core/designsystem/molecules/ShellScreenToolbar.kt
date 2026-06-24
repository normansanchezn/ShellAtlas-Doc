package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellElevation
import com.shelldocs.core.designsystem.tokens.ShellSpacing

private val ToolbarHeight = 72.dp
private val ToolbarSideMinWidth = 132.dp
private val ToolbarSideMaxWidth = 240.dp

/**
 * Shared workspace toolbar for desktop/web screens.
 * Keep at most two actions per side so page hierarchy stays stable.
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .shadow(elevation = ShellElevation.raised)
            .background(colors.background)
            .padding(horizontal = ShellSpacing.lg),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .widthIn(min = ToolbarSideMinWidth, max = ToolbarSideMaxWidth),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (leadingContent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                ) {
                    leadingContent()
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = ToolbarSideMinWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = ShellTheme.typography.pageTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .widthIn(min = ToolbarSideMinWidth, max = ToolbarSideMaxWidth),
            contentAlignment = Alignment.CenterEnd,
        ) {
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
}
