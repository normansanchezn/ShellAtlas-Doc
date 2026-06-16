package com.shelldocs.feature.assistant.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
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
                )
            }
            if (availability != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
                    modifier = Modifier.padding(end = ShellSpacing.sm),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (availability.isLlmReachable) colors.success else colors.warning),
                    )
                    Text(
                        text = if (availability.isLlmReachable) "AI available" else "Grounded mode",
                        style = ShellTheme.typography.caption,
                        color = if (availability.isLlmReachable) colors.success else colors.warning,
                    )
                }
            }
            if (knowledgeProgress != null && knowledgeProgress.total > 0) {
                ShellGhostButton(
                    text = if (activeCheckpointId != null) {
                        "KT: paso ${knowledgeProgress.completed + 1}/${knowledgeProgress.total}"
                    } else if (knowledgeProgress.completed >= knowledgeProgress.total) {
                        "KT completo · ${knowledgeProgress.percent}%"
                    } else {
                        "Iniciar Knowledge Transfer"
                    },
                    icon = IconBookOpen,
                    onClick = onStartKnowledgeTransfer,
                )
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
