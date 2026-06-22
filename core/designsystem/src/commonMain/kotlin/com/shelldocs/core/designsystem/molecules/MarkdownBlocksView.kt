package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
                is CodeBlock -> if (block.language.equals("mermaid", ignoreCase = true)) {
                    MermaidDiagramCard(diagram = MermaidParser.parse(block.code))
                } else {
                    Text(
                        text = block.code,
                        style = ShellTheme.typography.code,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ShellRadius.md))
                            .background(colors.surfaceSubtle)
                            .padding(ShellSpacing.md),
                    )
                }

                is TableBlock -> MarkdownTable(block)
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

@Composable
private fun MarkdownTable(block: TableBlock) {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.md))
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md)),
    ) {
        Row(modifier = Modifier.fillMaxWidth().background(colors.surfaceSubtle)) {
            block.headers.forEach { header ->
                Text(
                    text = header,
                    style = ShellTheme.typography.bodyStrong,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f).padding(ShellSpacing.sm),
                )
            }
        }
        block.rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = colors.border)) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = ShellTheme.typography.body,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f).padding(ShellSpacing.sm),
                    )
                }
            }
        }
    }
}
