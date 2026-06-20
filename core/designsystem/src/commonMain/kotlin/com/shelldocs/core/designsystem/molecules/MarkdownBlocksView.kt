package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.*

/** Renders parsed document blocks with the reader typography — the canonical document preview, shared by every feature. */
@Composable
fun MarkdownBlocksView(
    blocks: List<ContentBlock>,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
        blocks.forEach { block ->
            when (block) {
                is HeadingBlock -> Text(
                    text = block.text,
                    style = if (block.level <= 2) {
                        ShellTheme.typography.pageTitle
                    } else {
                        ShellTheme.typography.sectionTitle
                    },
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = if (block.level <= 2) ShellSpacing.sm else ShellSpacing.xs),
                )
                is ParagraphBlock -> ShellMarkdownText(
                    text = block.text,
                    style = ShellTheme.typography.body,
                    color = colors.textSecondary,
                )
                is ListBlock -> Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                    block.items.forEachIndexed { index, item ->
                        Row {
                            Text(
                                text = if (block.style == ListStyle.ORDERED) "${index + 1}.  " else "•  ",
                                style = ShellTheme.typography.body,
                                color = colors.textMuted,
                            )
                            ShellMarkdownText(
                                text = item,
                                style = ShellTheme.typography.body,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
                is CodeBlock -> Text(
                    text = block.code,
                    style = ShellTheme.typography.code,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.surfaceSubtle)
                        .padding(ShellSpacing.md),
                )
                is QuoteBlock -> Row {
                    Box(
                        modifier = Modifier
                            .padding(end = ShellSpacing.sm)
                            .background(colors.brand)
                            .fillMaxWidth(0.005f),
                    )
                    ShellMarkdownText(
                        text = block.text,
                        style = ShellTheme.typography.body,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}
