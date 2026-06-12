package com.shelldocs.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.icons.IconShellPecten
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.auth.presentation.AuthEffect
import com.shelldocs.feature.auth.presentation.AuthIntent
import com.shelldocs.feature.auth.presentation.AuthViewModel

/** Centered sign-in card with the ShellDoc brand mark. */
@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    isDemoMode: Boolean,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToWorkspace -> onSignedIn()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        ShellCard(elevated = true, modifier = Modifier.widthIn(max = 360.dp).padding(ShellSpacing.lg)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = IconShellPecten,
                        contentDescription = "ShellDoc",
                        tint = colors.onBrand,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.height(ShellSpacing.md))
                Text("ShellDoc", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(
                    "Knowledge Platform",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
                if (isDemoMode) {
                    Spacer(Modifier.height(ShellSpacing.sm))
                    Text(
                        "Demo mode: any corporate email and an 8+ character password will sign in.",
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                    )
                }
                Spacer(Modifier.height(ShellSpacing.xxl))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
                ) {
                    ShellTextField(
                        value = state.email,
                        onValueChange = { viewModel.onIntent(AuthIntent.EmailChanged(it)) },
                        placeholder = "Work email",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    ShellTextField(
                        value = state.password,
                        onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
                        placeholder = "Password",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (state.canSubmit) viewModel.onIntent(AuthIntent.Submit) },
                        ),
                        onSubmit = { if (state.canSubmit) viewModel.onIntent(AuthIntent.Submit) },
                    )
                }
                Spacer(Modifier.height(ShellSpacing.lg))
                ShellPrimaryButton(
                    text = if (state.isLoading) "Signing in..." else "Sign in",
                    onClick = { viewModel.onIntent(AuthIntent.Submit) },
                    enabled = state.canSubmit,
                    modifier = Modifier.width(160.dp),
                )
            }
        }

        if (state.isLoading) {
            ShellLoadingOverlay(message = "Signing you in...")
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(AuthIntent.DismissError) },
        )
    }
}
