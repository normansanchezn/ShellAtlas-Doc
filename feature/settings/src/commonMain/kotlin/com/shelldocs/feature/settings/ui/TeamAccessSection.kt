package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.molecules.ShellToggle
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.feature.settings.presentation.SettingsIntent
import com.shelldocs.feature.settings.presentation.SettingsState

/**
 * Team & Access: members with their Supabase-delegated roles. Owners can
 * cycle a member's role by tapping the role chip.
 */
@Composable
fun TeamAccessSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 560.dp)) {
        Text("Team & Access", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)

        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Team Members",
                        style = ShellTheme.typography.bodyStrong,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.canManageMembers) {
                        ShellPrimaryButton(text = "Invite member", onClick = {})
                    }
                }
                state.members.forEach { member ->
                    MemberRow(
                        member = member,
                        canManage = state.canManageMembers && !member.isCurrentUser && !state.isBusy,
                        onRoleChange = { newRole ->
                            onIntent(SettingsIntent.AssignRole(member.profile.id, newRole))
                        },
                    )
                }
            }
        }

        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.lg)) {
            Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg)) {
                AccessToggleRow(
                    title = "Two-factor authentication",
                    subtitle = "Require 2FA for all team members",
                    checked = state.twoFactorRequired,
                    onCheckedChange = { onIntent(SettingsIntent.SetTwoFactorRequired(it)) },
                )
                AccessToggleRow(
                    title = "Audit log",
                    subtitle = "Track all document changes and access events",
                    checked = state.auditLogEnabled,
                    onCheckedChange = { onIntent(SettingsIntent.SetAuditLogEnabled(it)) },
                    modifier = Modifier.padding(top = ShellSpacing.md),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = ShellSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            ShellPrimaryButton(
                text = if (state.isBusy) "Saving..." else "Save Changes",
                onClick = { onIntent(SettingsIntent.SaveChanges) },
                enabled = !state.isBusy,
            )
            state.saveMessage?.let { message ->
                Text(text = message, style = ShellTheme.typography.caption, color = colors.success)
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: TeamMember,
    canManage: Boolean,
    onRoleChange: (UserRole) -> Unit,
) {
    val colors = ShellTheme.colors
    var isPickerOpen by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShellAvatar(
                initials = member.profile.initials,
                size = 26.dp,
                color = if (member.isCurrentUser) colors.brand else colors.surfaceSubtle,
                contentColor = if (member.isCurrentUser) colors.onBrand else colors.textSecondary,
            )
            Column(modifier = Modifier.weight(1f).padding(start = ShellSpacing.md)) {
                Text(member.profile.fullName, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                Text(member.profile.team, style = ShellTheme.typography.caption, color = colors.textMuted)
            }
            RoleChip(
                role = member.profile.role,
                highlighted = member.profile.role == UserRole.OWNER,
                modifier = Modifier.clickable(enabled = canManage) { isPickerOpen = !isPickerOpen },
            )
        }
        if (isPickerOpen && canManage) {
            Row(
                modifier = Modifier.padding(top = ShellSpacing.xs, start = 38.dp),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
            ) {
                UserRole.entries.forEach { role ->
                    RoleChip(
                        role = role,
                        highlighted = role == member.profile.role,
                        modifier = Modifier.clickable {
                            isPickerOpen = false
                            if (role != member.profile.role) onRoleChange(role)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    role: UserRole,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    ShellBadge(
        text = role.displayName,
        contentColor = if (highlighted) colors.onBrand else colors.textSecondary,
        containerColor = if (highlighted) colors.brand else colors.surfaceSubtle,
        modifier = modifier,
    )
}

@Composable
private fun AccessToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(subtitle, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
        ShellToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
