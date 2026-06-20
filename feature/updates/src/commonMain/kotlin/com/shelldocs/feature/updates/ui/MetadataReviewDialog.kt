@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellDropdown
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.ApplicationVersionCatalog
import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.entity.document.DocumentAttributes

/** Review/complete metadata before publishing — accept, modify, or fill in missing values. */
@Composable
fun MetadataReviewDialog(
    attributes: DocumentAttributes,
    onCancel: () -> Unit,
    onApply: (DocumentAttributes) -> Unit,
) {
    var owner by remember(attributes) { mutableStateOf(attributes.owner) }
    var area by remember(attributes) { mutableStateOf(attributes.area) }
    var applicationVersion by remember(attributes) { mutableStateOf(attributes.applicationVersion) }
    var platform by remember(attributes) { mutableStateOf(attributes.platform) }
    var tags by remember(attributes) { mutableStateOf(attributes.tags.joinToString(", ")) }
    var team by remember(attributes) { mutableStateOf(attributes.team) }
    var module by remember(attributes) { mutableStateOf(attributes.module) }

    ShellDialog(
        title = "Review Metadata",
        onDismiss = onCancel,
        actions = {
            ShellGhostButton(text = "Cancel", onClick = onCancel)
            ShellPrimaryButton(
                text = "Apply Update",
                onClick = {
                    onApply(
                        attributes.copy(
                            owner = owner,
                            area = area,
                            applicationVersion = applicationVersion,
                            platform = platform,
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            team = team,
                            module = module,
                        ),
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            MetadataField("Owner") {
                ShellTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    placeholder = "Owner"
                )
            }
            MetadataField("Area") {
                ShellDropdown(
                    selected = area,
                    options = Area.entries,
                    label = { it.displayName },
                    onSelect = { area = it },
                    placeholder = "Select area"
                )
            }
            MetadataField("Application Version") {
                ShellDropdown(
                    selected = applicationVersion.ifBlank { null },
                    options = ApplicationVersionCatalog.selectableFrom(attributes.applicationVersion.ifBlank { null }),
                    label = { it },
                    onSelect = { applicationVersion = it },
                    placeholder = "Select version",
                )
            }
            MetadataField("Platform") {
                ShellTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    placeholder = "Platform"
                )
            }
            MetadataField("Tags") {
                ShellTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = "Comma-separated tags"
                )
            }
            MetadataField("Team") { ShellTextField(value = team, onValueChange = { team = it }, placeholder = "Team") }
            MetadataField("Module") {
                ShellTextField(
                    value = module,
                    onValueChange = { module = it },
                    placeholder = "Module"
                )
            }
        }
    }
}

@Composable
private fun MetadataField(label: String, field: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ShellSpacing.xxs)) {
        Text(text = label, style = ShellTheme.typography.caption, color = ShellTheme.colors.textMuted)
        field()
    }
}
