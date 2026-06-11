package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconSparkles
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability

/** Top strip: "AI Assistant — grounded on N documents" + availability dot. */
@Composable
fun AssistantHeader(
    indexedDocuments: Int,
    availability: AssistantAvailability?,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = modifier
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
                text = "Grounded on ShellDoc knowledge · $indexedDocuments documents indexed",
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
            )
        }
        if (availability != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
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
    }
}
