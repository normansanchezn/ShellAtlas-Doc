package com.shelldocs.core.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.domain.entity.updates.RiskLevel

/** Uppercase severity chip used in the Updates Pending triage table. */
@Composable
fun ShellRiskBadge(risk: RiskLevel, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val (content, container) = when (risk) {
        RiskLevel.CRITICAL -> colors.danger to colors.dangerSoft
        RiskLevel.HIGH -> colors.warning to colors.warningSoft
        RiskLevel.MEDIUM -> colors.brand to colors.surfaceSelected
        RiskLevel.LOW -> colors.info to colors.infoSoft
    }
    ShellBadge(
        text = risk.displayName.uppercase(),
        contentColor = content,
        containerColor = container,
        modifier = modifier,
    )
}
