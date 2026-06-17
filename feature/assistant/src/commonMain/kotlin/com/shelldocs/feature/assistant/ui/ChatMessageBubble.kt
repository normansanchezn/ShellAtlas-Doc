package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.icons.IconLanguage
import com.shelldocs.core.designsystem.icons.IconSparkles
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AnswerSource
import com.shelldocs.core.domain.entity.assistant.AssistantMessage
import com.shelldocs.core.domain.entity.assistant.MessageRole

/**
 * One chat turn. User questions render as yellow chips on the right;
 * assistant answers as cards with confidence header and source citations.
 */
@Composable
fun ChatMessageBubble(
    message: AssistantMessage,
    onSourceClick: (AnswerSource) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (message.role) {
        MessageRole.USER -> UserBubble(message, modifier)
        MessageRole.SYSTEM -> SystemBubble(message, modifier)
        MessageRole.ASSISTANT -> AssistantBubble(message, onSourceClick, modifier)
    }
}

@Composable
private fun UserBubble(message: AssistantMessage, modifier: Modifier) {
    val colors = ShellTheme.colors
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            text = message.markdown,
            style = ShellTheme.typography.bodyStrong,
            color = colors.onBrand,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(ShellRadius.md))
                .background(colors.brand)
                .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm),
        )
    }
}

@Composable
private fun SystemBubble(message: AssistantMessage, modifier: Modifier) {
    val colors = ShellTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = ShellSpacing.xs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = IconLanguage,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = message.markdown,
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
            modifier = Modifier.padding(start = ShellSpacing.xs),
        )
    }
}

@Composable
private fun AssistantBubble(message: AssistantMessage, onSourceClick: (AnswerSource) -> Unit, modifier: Modifier) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.fillMaxWidth().widthIn(max = 640.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            Icon(
                imageVector = IconSparkles,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(13.dp),
            )
            Text("ShellAtlas AI", style = ShellTheme.typography.label, color = colors.textSecondary)
            message.confidence?.let { confidence ->
                ConfidenceChip(confidence)
                if (message.sources.isNotEmpty()) {
                    Text(
                        text = "Based on ${message.sources.size} sources",
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(top = ShellSpacing.sm)
                .fillMaxWidth()
                .clip(RoundedCornerShape(ShellRadius.md))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md))
                .padding(ShellSpacing.lg),
        ) {
            AssistantRichContent(markdown = message.markdown)
        }
        if (message.sources.isNotEmpty()) {
            SourcesList(
                sources = message.sources,
                onSourceClick = onSourceClick,
                modifier = Modifier.padding(top = ShellSpacing.sm),
            )
        }
    }
}

@Composable
private fun ConfidenceChip(confidence: AnswerConfidence) {
    val colors = ShellTheme.colors
    val (text, content, container) = when (confidence) {
        AnswerConfidence.HIGH -> Triple("${confidence.percentage}% confidence", colors.success, colors.successSoft)
        AnswerConfidence.MEDIUM -> Triple("${confidence.percentage}% confidence", colors.warning, colors.warningSoft)
        AnswerConfidence.LOW -> Triple("${confidence.percentage}% confidence", colors.danger, colors.dangerSoft)
        AnswerConfidence.NOT_ENOUGH_INFORMATION -> Triple("Not enough information", colors.textMuted, colors.surfaceSubtle)
    }
    ShellBadge(text = text, contentColor = content, containerColor = container)
}
