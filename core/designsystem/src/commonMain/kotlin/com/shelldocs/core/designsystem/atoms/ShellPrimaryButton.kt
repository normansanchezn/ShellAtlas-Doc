package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Brand-yellow primary action ("Save Changes", "+ New", "Invite member"). */
@Composable
fun ShellPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = modifier
            .height(30.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.brand)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = ShellSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(text = text, style = ShellTheme.typography.label, color = colors.onBrand)
    }
}
