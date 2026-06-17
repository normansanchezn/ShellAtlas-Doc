package com.shelldocs.feature.assistant.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.icons.IconBookOpen
import com.shelldocs.core.designsystem.icons.IconSparkles
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress

/** Top strip: "AI Assistant — grounded on N documents" + availability dot + Knowledge Transfer entry point. */
@Composable
fun AssistantHeader(
    indexedDocuments: Int,
    availability: AssistantAvailability?,
    knowledgeProgress: KnowledgeProgress?,
    activeCheckpointId: String?,
    onStartKnowledgeTransfer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = IconSparkles,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(16.dp),
            )
            Column(modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm)) {
                Text("AI Assistant", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
                Text(
                    text = "Grounded on ShellAtlas knowledge · $indexedDocuments documents indexed",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    maxLines = 1,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
            ) {
                if (availability != null) {
                    AvailabilityStatusButton(availability)
                }
                if (knowledgeProgress != null && knowledgeProgress.total > 0) {
                    KnowledgeTransferButton(
                        progress = knowledgeProgress,
                        activeCheckpointId = activeCheckpointId,
                        onClick = onStartKnowledgeTransfer,
                    )
                }
            }
        }
        if (activeCheckpointId != null && knowledgeProgress != null && knowledgeProgress.total > 0) {
            KnowledgeTransferProgressBar(
                progress = knowledgeProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.xs),
            )
        }
    }
}

/** Compact dot button; tap reveals the full availability status as a popover. */
@Composable
private fun AvailabilityStatusButton(availability: AssistantAvailability) {
    val colors = ShellTheme.colors
    val density = LocalDensity.current
    var expanded by remember { mutableStateOf(false) }
    val statusColor = if (availability.isLlmReachable) colors.success else colors.warning
    val statusText = if (availability.isLlmReachable) "AI available" else "Grounded mode"
    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(ShellRadius.sm))
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor),
            )
        }
        if (expanded) {
            Popup(
                alignment = Alignment.BottomEnd,
                offset = with(density) { IntOffset(0, ShellSpacing.xs.roundToPx()) },
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 220.dp)
                        .shadow(4.dp, RoundedCornerShape(ShellRadius.md))
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.surface)
                        .border(Dp.Hairline, colors.border, RoundedCornerShape(ShellRadius.md))
                        .padding(ShellSpacing.sm),
                ) {
                    Text(text = statusText, style = ShellTheme.typography.caption, color = statusColor)
                }
            }
        }
    }
}

/** Icon-only Knowledge Transfer entry point; tapping immediately starts/continues the flow. */
@Composable
private fun KnowledgeTransferButton(
    progress: KnowledgeProgress,
    activeCheckpointId: String?,
    onClick: () -> Unit,
) {
    val colors = ShellTheme.colors
    val isComplete = progress.completed >= progress.total
    val tint = when {
        isComplete -> colors.success
        activeCheckpointId != null -> colors.brand
        else -> colors.textMuted
    }
    Box {
        ShellIconButton(
            icon = IconBookOpen,
            contentDescription = "Knowledge Transfer",
            onClick = onClick,
            tint = tint,
        )
        if (activeCheckpointId != null && !isComplete) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colors.brand),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${progress.completed + 1}",
                    style = ShellTheme.typography.caption,
                    color = colors.surface,
                )
            }
        }
    }
}

@Composable
private fun KnowledgeTransferProgressBar(
    progress: KnowledgeProgress,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val target = if (progress.total == 0) 0f else progress.completed.toFloat() / progress.total.toFloat()
    val fraction by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(ShellMotion.durationSlow, easing = ShellMotion.standard),
        label = "knowledgeTransferProgress",
    )
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(ShellRadius.full))
            .background(colors.surfaceSubtle),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(ShellRadius.full))
                .background(colors.brand),
        )
    }
}
