package com.shelldocs.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.shelldocs.app.ui.RAIL_LAYOUT_MIN_WIDTH_DP
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.gestures.detectTapGestures
import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.AppContainer
import com.shelldocs.app.ui.WorkspaceShell
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.theme.ShellDocsTheme
import com.shelldocs.core.designsystem.tokens.ShellTypography
import com.shelldocs.feature.auth.ui.SignInScreen

/**
 * Multiplatform root: theme, auth gate (the AUTH clean-architecture example)
 * and the adaptive workspace shell.
 *
 * [themePrefs] is injected by each platform entry point so the preference can
 * be stored in the appropriate native key-value store.  On first launch (or
 * when [themePrefs] returns null) the initial theme is read from the host
 * OS via [isSystemInDarkTheme].
 */
@Composable
fun App(
    config: AppConfig = AppConfig(),
    themePrefs: ThemePreferences = NoOpThemePreferences,
    sessionPrefs: SessionPreferences = NoOpSessionPreferences,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val container = remember(config) { AppContainer(config, sessionPrefs) }
        val systemDark = isSystemInDarkTheme()
        val focusManager = LocalFocusManager.current
        val widthDp = maxWidth.value.toInt()
        val isMobile = widthDp < RAIL_LAYOUT_MIN_WIDTH_DP
        val baseTypography = if (isMobile) ShellTypography.mobile() else ShellTypography.default()
        var isDarkTheme by remember { mutableStateOf(themePrefs.load() ?: systemDark) }
        var textScale by remember { mutableFloatStateOf(1f) }

        ShellDocsTheme(darkTheme = isDarkTheme, typography = baseTypography, textScale = textScale) {
            val session by container.authRepository.session.collectAsState()
            // BoxWithConstraints intentionally has NO windowInsetsPadding here.
            // The login screen background bleeds edge-to-edge (behind Dynamic Island
            // and home indicator). WorkspaceShell owns its own inset padding.
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(DemoTestTags.WorkspaceRoot)
                    .pointerInput(focusManager) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .onPreviewKeyEvent { event ->
                        if (isMobile || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        val mod = event.isMetaPressed || event.isCtrlPressed
                        if (!mod) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.Equals -> { textScale = (textScale + ZOOM_STEP).coerceAtMost(MAX_ZOOM); true }
                            Key.Minus -> { textScale = (textScale - ZOOM_STEP).coerceAtLeast(MIN_ZOOM); true }
                            Key.Zero -> { textScale = 1f; true }
                            else -> false
                        }
                    },
            ) {
                if (session == null) {
                    val authViewModel = remember(container) { container.authViewModel() }
                    DisposableEffect(authViewModel) { onDispose(authViewModel::clear) }
                    SignInScreen(
                        viewModel = authViewModel,
                        isDemoMode = config.isDemoMode,
                        isMobile = isMobile,
                        onSignedIn = { /* session flow drives the switch */ },
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    ) {
                        WorkspaceShell(
                            container = container,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = {
                                val next = !isDarkTheme
                                isDarkTheme = next
                                themePrefs.save(next)
                            },
                            onSignedOut = { /* session flow drives the switch */ },
                            availableWidthDp = widthDp,
                        )
                    }
                }
            }
        }
    }
}

private const val ZOOM_STEP = 0.1f
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 2.0f
