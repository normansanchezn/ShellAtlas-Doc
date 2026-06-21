package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.*
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellColorScheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.*
import com.shelldocs.feature.updates.UpdatesStringRes

/** Admin-only targeted metadata editor for the fields the classifier flagged. */
@Composable
fun EditMetadataDialog(
    issue: DocumentClassificationResult,
    onDismiss: () -> Unit,
    onAccept: (documentId: String, assignments: List<MetadataAssignment>) -> Unit,
) {
    val editableAttributes = remember(issue) { issue.missingAttributes.distinct() }
    val suggestionsByAttribute = remember(issue) { issue.suggestions.associateBy { it.attribute } }
    val values = remember(issue) {
        mutableStateMapOf<MetadataAttribute, String>().apply {
            editableAttributes.forEach { attribute ->
                this[attribute] = suggestionsByAttribute[attribute]
                    ?.takeIf { it.confidencePercent >= 50 }
                    ?.suggestedValue
                    .orEmpty()
            }
        }
    }
    val isValid = editableAttributes.all { attribute ->
        val value = values[attribute].orEmpty().trim()
        if (attribute == MetadataAttribute.AREA) {
            Area.fromKey(value) != null
        } else {
            value.isNotBlank()
        }
    }

    ShellDialog(
        title = UpdatesStringRes.METADATA_REVIEW_TITLE,
        onDismiss = onDismiss,
        actions = {
            ShellGhostButton(text = UpdatesStringRes.CANCEL, onClick = onDismiss)
            ShellPrimaryButton(
                text = UpdatesStringRes.ACCEPT,
                onClick = {
                    onAccept(
                        issue.documentId,
                        editableAttributes.mapNotNull { attribute ->
                            values[attribute]
                                ?.trim()
                                ?.takeIf { it.isNotBlank() }
                                ?.let { MetadataAssignment(attribute = attribute, value = it) }
                        },
                    )
                },
                enabled = isValid,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 460.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Text(
                text = UpdatesStringRes.METADATA_REVIEW_DESCRIPTION,
                style = ShellTheme.typography.caption,
                color = ShellTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = ShellSpacing.sm),
            )
            editableAttributes.forEach { attribute ->
                MetadataEditField(
                    attribute = attribute,
                    value = values[attribute].orEmpty(),
                    suggestion = suggestionsByAttribute[attribute]?.suggestedValue,
                    confidencePercent = suggestionsByAttribute[attribute]?.confidencePercent,
                    onValueChange = { values[attribute] = it },
                )
            }
        }
    }
}

@Composable
private fun MetadataEditField(
    attribute: MetadataAttribute,
    value: String,
    suggestion: String?,
    confidencePercent: Int?,
    onValueChange: (String) -> Unit,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.xxs),
    ) {
        Text(
            text = attributeLabel(attribute),
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
        )
        if (!suggestion.isNullOrBlank() && confidencePercent != null) {
            MetadataSuggestionHint(
                suggestion = suggestion,
                confidencePercent = confidencePercent,
                colors = colors,
            )
        }
        when (attribute) {
            MetadataAttribute.AREA -> ShellDropdown(
                selected = Area.entries.firstOrNull { it.displayName == value },
                options = Area.entries,
                label = { it.displayName },
                onSelect = { onValueChange(it.displayName) },
                placeholder = UpdatesStringRes.PLACEHOLDER_SELECT_AREA,
            )

            MetadataAttribute.APPLICATION_VERSION -> ShellDropdown(
                selected = value.ifBlank { null },
                options = ApplicationVersionCatalog.selectableFrom(value.ifBlank { null }),
                label = { it },
                onSelect = onValueChange,
                placeholder = UpdatesStringRes.PLACEHOLDER_SELECT_VERSION,
            )

            MetadataAttribute.TAGS -> ShellTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = UpdatesStringRes.PLACEHOLDER_COMMA_SEPARATED_TAGS,
            )

            else -> ShellTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = attribute.displayName,
            )
        }
    }
}

@Composable
private fun MetadataSuggestionHint(
    suggestion: String,
    confidencePercent: Int,
    colors: ShellColorScheme,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = "${UpdatesStringRes.LABEL_SUGGESTED}: $suggestion",
            style = ShellTheme.typography.caption,
            color = colors.textSecondary,
        )
        ShellBadge(
            text = "$confidencePercent%",
            contentColor = when {
                confidencePercent >= 80 -> colors.success
                confidencePercent >= 50 -> colors.warning
                else -> colors.danger
            },
            containerColor = colors.surfaceSubtle,
        )
    }
}

private fun attributeLabel(attribute: MetadataAttribute): String = when (attribute) {
    MetadataAttribute.OWNER -> UpdatesStringRes.FIELD_OWNER
    MetadataAttribute.AREA -> UpdatesStringRes.FIELD_AREA
    MetadataAttribute.APPLICATION_VERSION -> UpdatesStringRes.FIELD_APPLICATION_VERSION
    MetadataAttribute.PLATFORM -> UpdatesStringRes.FIELD_PLATFORM
    MetadataAttribute.TAGS -> UpdatesStringRes.FIELD_TAGS
    MetadataAttribute.TEAM -> UpdatesStringRes.FIELD_TEAM
    MetadataAttribute.MODULE -> UpdatesStringRes.FIELD_MODULE
    MetadataAttribute.DOCUMENT_TYPE -> attribute.displayName
}
