package com.shelldocs.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellBrandBadge
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.auth.presentation.AuthEffect
import com.shelldocs.feature.auth.presentation.AuthIntent
import com.shelldocs.feature.auth.presentation.AuthViewModel

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    isDemoMode: Boolean,
    isDarkTheme: Boolean,
    onSignedIn: () -> Unit,
    isMobile: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val animateBackground = !isInstrumentedUiTestRuntime()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToWorkspace -> onSignedIn()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(DemoTestTags.SignInRoot),
    ) {
        ShellLoginBackground(
            isDarkTheme = isDarkTheme,
            animate = animateBackground,
        )

        if (isMobile) {
            MobileSignInContent(
                state = state,
                viewModel = viewModel,
                isDemoMode = isDemoMode,
            )
        } else {
            DesktopSignInContent(
                state = state,
                viewModel = viewModel,
                isDemoMode = isDemoMode,
            )
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

@Composable
private fun MobileSignInContent(
    state: com.shelldocs.feature.auth.presentation.AuthState,
    viewModel: AuthViewModel,
    isDemoMode: Boolean,
) {
    val colors = ShellTheme.colors
    val typography = ShellTheme.typography
    val bodyColor = colors.textPrimary
    val supportingColor = colors.textMuted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShellSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(ShellSpacing.xxxl))
        ShellBrandBadge(size = 52.dp, iconSize = 30.dp)
        Spacer(Modifier.height(ShellSpacing.lg))
        Text("ShellAtlas", style = typography.displayTitle, color = bodyColor)

        if (isDemoMode) {
            Spacer(Modifier.height(ShellSpacing.sm))
            Text(
                "Demo mode: any corporate email and an 8+ character password will sign in.",
                style = typography.caption,
                color = supportingColor,
            )
        }

        Spacer(Modifier.height(ShellSpacing.xxxl))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            ShellTextField(
                value = state.email,
                onValueChange = { viewModel.onIntent(AuthIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.SignInEmail),
                placeholder = "Work email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            ShellTextField(
                value = state.password,
                onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.SignInPassword),
                placeholder = "Password",
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (state.canSubmit) viewModel.onIntent(AuthIntent.Submit) },
                ),
                onSubmit = { if (state.canSubmit) viewModel.onIntent(AuthIntent.Submit) },
            )
        }

        Spacer(Modifier.height(ShellSpacing.xl))

        ShellPrimaryButton(
            text = if (state.isLoading) "Signing in..." else "Sign in",
            onClick = { viewModel.onIntent(AuthIntent.Submit) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.SignInSubmit),
        )

        Spacer(Modifier.height(ShellSpacing.xxxl))
    }
}

@Composable
private fun DesktopSignInContent(
    state: com.shelldocs.feature.auth.presentation.AuthState,
    viewModel: AuthViewModel,
    isDemoMode: Boolean,
) {
    val colors = ShellTheme.colors

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(ShellSpacing.lg),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 28.dp, vertical = 22.dp)
                    .blur(42.dp)
                    .clip(RoundedCornerShape(ShellRadius.lg))
                    .background(
                        if (colors.isDark) colors.brand.copy(alpha = 0.08f) else colors.info.copy(alpha = 0.045f),
                    ),
            )
            ShellCard(
                elevated = false,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ShellBrandBadge(size = 44.dp, iconSize = 26.dp)
                    Spacer(Modifier.height(ShellSpacing.md))
                    Text("ShellAtlas", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
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
                            modifier = Modifier.testTag(DemoTestTags.SignInEmail),
                            placeholder = "Work email",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        ShellTextField(
                            value = state.password,
                            onValueChange = { viewModel.onIntent(AuthIntent.PasswordChanged(it)) },
                            modifier = Modifier.testTag(DemoTestTags.SignInPassword),
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
                        modifier = Modifier.width(160.dp).testTag(DemoTestTags.SignInSubmit),
                    )
                }
            }
        }
    }
}
