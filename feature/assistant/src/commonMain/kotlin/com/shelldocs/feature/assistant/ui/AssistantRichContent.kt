package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.molecules.ShellMarkdownText
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import kotlin.math.max

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

@Composable
private fun MermaidDiagramCard(diagram: MermaidDiagram) {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md))
            .padding(ShellSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        when (diagram) {
            is MermaidDiagram.Flowchart -> FlowchartDiagram(diagram)
            is MermaidDiagram.Sequence -> SequenceDiagram(diagram)
            is MermaidDiagram.Gantt -> GanttDiagram(diagram)
            is MermaidDiagram.Unsupported -> CodeBlock(diagram.source)
        }
    }
}

@Composable
private fun FlowchartDiagram(diagram: MermaidDiagram.Flowchart) {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        diagram.nodes.forEachIndexed { index, node ->
            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ShellRadius.md))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md))
                    .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = node,
                    style = ShellTheme.typography.bodyStrong,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            if (index < diagram.nodes.lastIndex) {
                Text(
                    text = "\u2193",
                    style = ShellTheme.typography.sectionTitle,
                    color = colors.brand,
                )
            }
        }
    }
}

@Composable
private fun SequenceDiagram(diagram: MermaidDiagram.Sequence) {
    val colors = ShellTheme.colors
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
            diagram.participants.forEach { participant ->
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .clip(RoundedCornerShape(ShellRadius.sm))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.sm))
                        .padding(vertical = ShellSpacing.sm, horizontal = ShellSpacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = participant,
                        style = ShellTheme.typography.label,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        diagram.messages.forEach { message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                diagram.participants.forEachIndexed { index, participant ->
                    val content = when (index) {
                        message.fromIndex -> "${participant}\n\u2500\u2500\u25b6"
                        message.toIndex -> "\u25c0\u2500\u2500\n${participant}"
                        else -> "\u2502"
                    }
                    Box(
                        modifier = Modifier.width(150.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = content,
                            style = ShellTheme.typography.caption,
                            color = if (index == message.fromIndex || index == message.toIndex) colors.brand else colors.textMuted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Text(
                text = message.label,
                style = ShellTheme.typography.body,
                color = colors.textSecondary,
                modifier = Modifier.padding(start = ShellSpacing.xs),
            )
        }
    }
}

@Composable
private fun GanttDiagram(diagram: MermaidDiagram.Gantt) {
    val colors = ShellTheme.colors
    val scroll = rememberScrollState()
    val maxDuration = max(1, diagram.tasks.maxOfOrNull { it.durationDays } ?: 1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        diagram.tasks.forEach { task ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
            ) {
                Text(
                    text = task.label,
                    style = ShellTheme.typography.body,
                    color = colors.textPrimary,
                    modifier = Modifier.width(180.dp),
                )
                Box(
                    modifier = Modifier
                        .width((260 * task.durationDays / maxDuration).dp.coerceAtLeast(80.dp))
                        .height(24.dp)
                        .clip(RoundedCornerShape(ShellRadius.sm))
                        .background(colors.brand.copy(alpha = 0.18f))
                        .border(1.dp, colors.brand.copy(alpha = 0.5f), RoundedCornerShape(ShellRadius.sm)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "${task.startLabel}  •  ${task.durationDays}d",
                        style = ShellTheme.typography.caption,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = ShellSpacing.sm),
                    )
                }
            }
        }
    }
}

private sealed interface AssistantBlock {
    data class Heading(val text: String) : AssistantBlock
    data class Paragraph(val text: String) : AssistantBlock
    data class Bullet(val text: String) : AssistantBlock
    data class Code(val text: String) : AssistantBlock
    data class Mermaid(val raw: String) : AssistantBlock
}

private sealed interface MermaidDiagram {
    data class Flowchart(val nodes: List<String>) : MermaidDiagram

    data class Sequence(
        val participants: List<String>,
        val messages: List<Message>,
    ) : MermaidDiagram

    data class Gantt(val tasks: List<Task>) : MermaidDiagram

    data class Unsupported(val source: String) : MermaidDiagram

    data class Message(
        val fromIndex: Int,
        val toIndex: Int,
        val label: String,
    )

    data class Task(
        val label: String,
        val startLabel: String,
        val durationDays: Int,
    )
}

private object MermaidParser {
    fun parse(raw: String): MermaidDiagram {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return MermaidDiagram.Unsupported(raw)
        return when {
            lines.first().startsWith("flowchart") -> parseFlowchart(lines.drop(1), raw)
            lines.first().startsWith("sequenceDiagram") -> parseSequence(lines.drop(1), raw)
            lines.first().startsWith("gantt") -> parseGantt(lines.drop(1), raw)
            else -> MermaidDiagram.Unsupported(raw)
        }
    }

    private fun parseFlowchart(lines: List<String>, raw: String): MermaidDiagram {
        val labels = lines
            .mapNotNull { line ->
                Regex("""[A-Z0-9]+\[(.+)]""").find(line)?.groupValues?.getOrNull(1)
            }
            .map(::cleanupLabel)
        return if (labels.isNotEmpty()) MermaidDiagram.Flowchart(labels) else MermaidDiagram.Unsupported(raw)
    }

    private fun parseSequence(lines: List<String>, raw: String): MermaidDiagram {
        val participants = mutableListOf<String>()
        val messages = mutableListOf<MermaidDiagram.Message>()
        lines.forEach { line ->
            when {
                line.startsWith("participant ") -> {
                    val label = line.substringAfter(" as ", line.substringAfter("participant ")).trim()
                    participants += cleanupLabel(label)
                }

                "->>" in line || "-->>" in line -> {
                    val match = Regex("""([A-Za-z0-9_]+)(?:-+>>)([A-Za-z0-9_]+):\s*(.+)""").find(line) ?: return@forEach
                    val from = match.groupValues[1]
                    val to = match.groupValues[2]
                    val label = cleanupLabel(match.groupValues[3])
                    val fromIndex = participants.indexOfFirst { it.equals(from, ignoreCase = true) || it.contains(from, ignoreCase = true) }
                        .takeIf { it >= 0 } ?: 0
                    val toIndex = participants.indexOfFirst { it.equals(to, ignoreCase = true) || it.contains(to, ignoreCase = true) }
                        .takeIf { it >= 0 } ?: (participants.lastIndex).coerceAtLeast(0)
                    messages += MermaidDiagram.Message(fromIndex, toIndex, label)
                }
            }
        }
        return if (participants.isNotEmpty() && messages.isNotEmpty()) {
            MermaidDiagram.Sequence(participants, messages)
        } else {
            MermaidDiagram.Unsupported(raw)
        }
    }

    private fun parseGantt(lines: List<String>, raw: String): MermaidDiagram {
        val tasks = lines
            .filter { ":" in it && !it.startsWith("title") && !it.startsWith("dateFormat") && !it.startsWith("axisFormat") && !it.startsWith("section") }
            .mapNotNull { line ->
                val parts = line.split(":")
                if (parts.size < 2) return@mapNotNull null
                val label = cleanupLabel(parts.first())
                val durationMatch = Regex("""(\d+)d""").find(line)
                MermaidDiagram.Task(
                    label = label,
                    startLabel = line.substringAfterLast(",").trim().takeIf { it.endsWith("d").not() } ?: "Scheduled",
                    durationDays = durationMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1,
                )
            }
        return if (tasks.isNotEmpty()) MermaidDiagram.Gantt(tasks) else MermaidDiagram.Unsupported(raw)
    }

    private fun cleanupLabel(value: String): String = value
        .replace("\"", "")
        .replace("'", "")
        .trim()
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
