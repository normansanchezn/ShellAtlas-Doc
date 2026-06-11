package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** AI Assistant: engine strategy and grounding rules (read-only summary). */
@Composable
fun AiAssistantSection(modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 520.dp)) {
        Text("AI Assistant", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
            ) {
                SettingInfoRow("Engine", "Local LLM (Ollama) with grounded fallback")
                SettingInfoRow("Endpoint", "http://127.0.0.1:11434 · llama3.2")
                SettingInfoRow("Grounding", "Answers cite only indexed documents")
                SettingInfoRow("Cache", "assistant_intelligence table (Supabase)")
            }
        }
    }
}

@Composable
private fun SettingInfoRow(label: String, value: String) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = ShellTheme.typography.label, color = colors.textMuted)
        Text(text = value, style = ShellTheme.typography.label, color = colors.textPrimary)
    }
}
