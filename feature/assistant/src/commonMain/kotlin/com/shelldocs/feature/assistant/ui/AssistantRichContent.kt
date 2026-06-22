package com.shelldocs.feature.assistant.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellBrandBadge
import com.shelldocs.core.designsystem.molecules.MermaidDiagramCard
import com.shelldocs.core.designsystem.molecules.MermaidParser
import com.shelldocs.core.designsystem.molecules.ShellMarkdownText
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import kotlinx.coroutines.delay

@Composable
fun AssistantRichContent(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        parseBlocks(markdown).forEach { block ->
            when (block) {
                is AssistantBlock.Heading -> Text(
                    text = block.text,
                    style = ShellTheme.typography.sectionTitle,
                    color = ShellTheme.colors.textPrimary,
                )

                is AssistantBlock.Bullet -> Row {
                    Text(
                        text = "\u2022  ",
                        style = ShellTheme.typography.body,
                        color = ShellTheme.colors.textMuted,
                    )
                    ShellMarkdownText(
                        text = block.text,
                        style = ShellTheme.typography.body,
                        color = ShellTheme.colors.textSecondary,
                    )
                }

                is AssistantBlock.Paragraph -> ShellMarkdownText(
                    text = block.text,
                    style = ShellTheme.typography.body,
                    color = ShellTheme.colors.textSecondary,
                )

                is AssistantBlock.Mermaid -> MermaidDiagramCard(diagram = MermaidParser.parse(block.raw))

                is AssistantBlock.Code -> CodeBlock(block.text)
            }
        }
    }
}

/**
 * Opening greeting layout: logo above centered text, with the capability
 * bullets collapsed into a single chip that cycles through them instead of
 * a static list, so the welcome state stays short while still listing what
 * the assistant can do.
 */
@Composable
fun AssistantWelcomeContent(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = parseBlocks(markdown)
    val bullets = blocks.filterIsInstance<AssistantBlock.Bullet>().map { it.text }
    var chipPlaced = false

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        WelcomeLogo()
        blocks.forEach { block ->
            when (block) {
                is AssistantBlock.Heading -> Text(
                    text = block.text,
                    style = ShellTheme.typography.sectionTitle.copy(textAlign = TextAlign.Center),
                    color = ShellTheme.colors.textPrimary,
                )

                is AssistantBlock.Paragraph -> ShellMarkdownText(
                    text = block.text,
                    style = ShellTheme.typography.body.copy(textAlign = TextAlign.Center),
                    color = ShellTheme.colors.textSecondary,
                )

                is AssistantBlock.Bullet -> if (!chipPlaced) {
                    chipPlaced = true
                    RotatingCapabilityChip(options = bullets)
                }

                is AssistantBlock.Mermaid -> MermaidDiagramCard(diagram = MermaidParser.parse(block.raw))

                is AssistantBlock.Code -> CodeBlock(block.text)
            }
        }
    }
}

@Composable
private fun WelcomeLogo() {
    ShellBrandBadge(size = 56.dp, iconSize = 32.dp)
}

/** Cycles through [options] one at a time, crossfading on each change, forever. */
@Composable
private fun RotatingCapabilityChip(options: List<String>, modifier: Modifier = Modifier) {
    if (options.isEmpty()) return
    val colors = ShellTheme.colors
    val isInstrumentedRuntime = isInstrumentedUiTestRuntime()
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(options, isInstrumentedRuntime) {
        if (isInstrumentedRuntime) {
            index = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(2600)
            index = (index + 1) % options.size
        }
    }

    Crossfade(
        targetState = index,
        animationSpec = tween(ShellMotion.durationSlow, easing = ShellMotion.standard),
        modifier = modifier,
    ) { current ->
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(ShellRadius.full))
                .background(colors.brand.copy(alpha = 0.12f))
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = options[current],
                style = ShellTheme.typography.bodyStrong.copy(textAlign = TextAlign.Center),
                color = colors.brand,
            )
        }
    }
}

@Composable
private fun CodeBlock(text: String) {
    val colors = ShellTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md))
            .padding(ShellSpacing.md),
    ) {
        Text(
            text = text,
            style = ShellTheme.typography.code,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private sealed interface AssistantBlock {
    data class Heading(val text: String) : AssistantBlock
    data class Paragraph(val text: String) : AssistantBlock
    data class Bullet(val text: String) : AssistantBlock
    data class Code(val text: String) : AssistantBlock
    data class Mermaid(val raw: String) : AssistantBlock
}

private fun parseBlocks(markdown: String): List<AssistantBlock> {
    val blocks = mutableListOf<AssistantBlock>()
    val lines = markdown.lines()
    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        when {
            line.startsWith("```") -> {
                val language = line.removePrefix("```").trim()
                val buffer = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].startsWith("```")) {
                    buffer += lines[index]
                    index++
                }
                val content = buffer.joinToString("\n").trim()
                blocks += if (language.equals("mermaid", ignoreCase = true)) {
                    AssistantBlock.Mermaid(content)
                } else {
                    AssistantBlock.Code(content)
                }
            }

            line.startsWith("### ") || line.startsWith("## ") || line.startsWith("# ") ->
                blocks += AssistantBlock.Heading(line.trimStart('#', ' ').trim())

            line.trimStart().startsWith("- ") ->
                blocks += AssistantBlock.Bullet(line.trimStart().removePrefix("- ").trim())

            line.isBlank() -> Unit

            else -> blocks += AssistantBlock.Paragraph(line.trim())
        }
        index++
    }
    return blocks
}
