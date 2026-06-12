package com.shelldocs.feature.assistant.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.icons.IconPlus
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.assistant.Conversation
import kotlin.time.ExperimentalTime

/** Left rail listing stored assistant threads, newest first. */
@Composable
fun ConversationsPanel(
    conversations: List<Conversation>,
    activeConversationId: String?,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface)
            .border(0.5.dp, colors.border, RoundedCornerShape(0.dp))
            .padding(vertical = ShellSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = ShellSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShellSectionLabel(text = "Conversations", modifier = Modifier.weight(1f))
            ShellIconButton(icon = IconPlus, contentDescription = "New conversation", onClick = onNew)
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.sm)) {
            items(conversations.size, key = { conversations[it].id }) { index ->
                val conversation = conversations[index]
                val isActive = conversation.id == activeConversationId
                val background by animateColorAsState(
                    targetValue = if (isActive) colors.surfaceSelected else colors.surface,
                    animationSpec = tween(ShellMotion.durationMedium),
                    label = "conversationItemBackground",
                )
                val titleColor by animateColorAsState(
                    targetValue = if (isActive) colors.brand else colors.textSecondary,
                    animationSpec = tween(ShellMotion.durationMedium),
                    label = "conversationItemTitle",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ShellSpacing.sm, vertical = 1.dp)
                        .clip(RoundedCornerShape(ShellRadius.sm))
                        .background(background)
                        .clickable { onSelect(conversation.id) }
                        .padding(horizontal = ShellSpacing.sm, vertical = 6.dp),
                ) {
                    Text(
                        text = conversation.title,
                        style = ShellTheme.typography.label,
                        color = titleColor,
                        maxLines = 1,
                    )
                    Text(
                        text = relativeDayLabel(conversation),
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun relativeDayLabel(conversation: Conversation): String {
    val isoDate = conversation.updatedAt.toString().substringBefore('T')
    return isoDate
}
