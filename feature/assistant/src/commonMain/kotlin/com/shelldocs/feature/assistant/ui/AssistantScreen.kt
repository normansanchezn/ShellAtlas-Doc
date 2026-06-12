package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconSparkles
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.assistant.presentation.AssistantEffect
import com.shelldocs.feature.assistant.presentation.AssistantIntent
import com.shelldocs.feature.assistant.presentation.AssistantViewModel

/**
 * AI Assistant page: conversations rail (wide layouts), header with
 * grounding stats, message thread and the ask bar.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    isWide: Boolean,
    onOpenDocument: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel) {
        viewModel.onIntent(AssistantIntent.Initialize)
        viewModel.effects.collect { effect ->
            when (effect) {
                AssistantEffect.ScrollToLatestMessage -> {
                    val lastIndex = viewModel.currentState.messages.size - 1
                    if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
                }
            }
        }
    }

    Row(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (isWide) {
            ConversationsPanel(
                conversations = state.conversations,
                activeConversationId = state.activeConversationId,
                onSelect = { viewModel.onIntent(AssistantIntent.SelectConversation(it)) },
                onNew = { viewModel.onIntent(AssistantIntent.StartNewConversation) },
                modifier = Modifier.width(220.dp).fillMaxHeight(),
            )
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            AssistantHeader(
                indexedDocuments = state.indexedDocuments,
                availability = state.availability,
                knowledgeProgress = state.knowledgeProgress,
                activeCheckpointId = state.activeCheckpointId,
                onStartKnowledgeTransfer = { viewModel.onIntent(AssistantIntent.StartKnowledgeTransfer) },
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.messages.isEmpty()) {
                    AssistantEmptyThread(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = ShellSpacing.xxl,
                            vertical = ShellSpacing.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
                    ) {
                        items(state.messages.size, key = { state.messages[it].id }) { index ->
                            ChatMessageBubble(
                                message = state.messages[index],
                                onSourceClick = { source -> onOpenDocument(source.documentId) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (state.isAnswering) {
                            item("typing") { TypingIndicator() }
                        }
                    }
                }
            }
            ChatInputBar(
                value = state.input,
                canSend = state.canSend,
                onValueChange = { viewModel.onIntent(AssistantIntent.InputChanged(it)) },
                onSend = { viewModel.onIntent(AssistantIntent.SendQuestion) },
            )
        }

        if (state.isInitializing) {
            ShellLoadingOverlay(message = "Preparing assistant...")
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(AssistantIntent.DismissError) },
        )
    }
}

@Composable
private fun AssistantEmptyThread(modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = IconSparkles,
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = "Ask about your documentation",
            style = ShellTheme.typography.bodyStrong,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = ShellSpacing.md),
        )
        Text(
            text = "Flows, processes, token rules, releases — answers are grounded on indexed docs.",
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
            modifier = Modifier.padding(top = ShellSpacing.xs),
        )
    }
}
