package com.shelldocs.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.AppContainer
import com.shelldocs.app.ui.RAIL_LAYOUT_MIN_WIDTH_DP
import com.shelldocs.app.ui.WorkspaceShell
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.i18n.LocalAppStrings
import com.shelldocs.core.designsystem.i18n.stringsFor
import com.shelldocs.core.designsystem.molecules.ShellLottieLoader
import com.shelldocs.core.designsystem.theme.ShellDocsTheme
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellTypography
import com.shelldocs.core.domain.entity.auth.AppLanguage
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
        var isAppLaunching by remember { mutableStateOf(true) }
        LaunchedEffect(container) {
            container.runStartupDiagnostics()
            isAppLaunching = false
        }
        val systemDark = isSystemInDarkTheme()
        val focusManager = LocalFocusManager.current
        val widthDp = maxWidth.value.toInt()
        val isMobile = widthDp < RAIL_LAYOUT_MIN_WIDTH_DP
        val baseTypography = if (isMobile) ShellTypography.mobile() else ShellTypography.default()
        var isDarkTheme by remember { mutableStateOf(themePrefs.load() ?: systemDark) }
        var userZoom by remember { mutableFloatStateOf(1f) }
        // Adaptive scaling by available width, the closest cross-platform proxy
        // we have for display resolution: 1080p-class windows stay at 100%,
        // 1440p-class at 110%, 4K-class at 120%. Manual zoom (Cmd +/-) layers on top.
        val resolutionScale = when {
            widthDp >= 2880 -> 1.2f
            widthDp >= 1920 -> 1.1f
            else -> 1f
        }
        val textScale = resolutionScale * userZoom

        ShellDocsTheme(darkTheme = isDarkTheme, typography = baseTypography, textScale = textScale) {
            val session by container.authRepository.session.collectAsState()
            val strings = stringsFor(session?.user?.language ?: AppLanguage.ENGLISH)
            CompositionLocalProvider(LocalAppStrings provides strings) {
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
                            Key.Equals -> {
                                userZoom = (userZoom + ZOOM_STEP).coerceAtMost(MAX_ZOOM); true
                            }

                            Key.Minus -> {
                                userZoom = (userZoom - ZOOM_STEP).coerceAtLeast(MIN_ZOOM); true
                            }

                            Key.Zero -> {
                                userZoom = 1f; true
                            }
                            else -> false
                        }
                    },
            ) {
                if (isAppLaunching) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(ShellTheme.colors.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        ShellLottieLoader()
                    }
                } else if (session == null) {
                    val authViewModel = remember(container) { container.authViewModel() }
                    DisposableEffect(authViewModel) { onDispose(authViewModel::clear) }
                    SignInScreen(
                        viewModel = authViewModel,
                        isDemoMode = config.isDemoMode,
                        isDarkTheme = isDarkTheme,
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
}

private const val ZOOM_STEP = 0.1f
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 2.0f
