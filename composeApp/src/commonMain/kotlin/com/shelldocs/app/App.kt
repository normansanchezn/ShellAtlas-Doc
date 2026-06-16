package com.shelldocs.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.shelldocs.app.di.AppConfig
import com.shelldocs.app.di.AppContainer
import com.shelldocs.app.ui.WorkspaceShell
import com.shelldocs.core.designsystem.theme.ShellDocsTheme
import com.shelldocs.feature.auth.ui.SignInScreen

/**
 * Multiplatform root: theme, auth gate (the AUTH clean-architecture example)
 * and the adaptive workspace shell.
 */
@Composable
fun App(config: AppConfig = AppConfig()) {
    val container = remember(config) { AppContainer(config) }
    var isDarkTheme by remember { mutableStateOf(false) }
    var textScale by remember { mutableFloatStateOf(1f) }

    ShellDocsTheme(darkTheme = isDarkTheme, textScale = textScale) {
        val session by container.authRepository.session.collectAsState()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
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
                    onSignedIn = { /* session flow drives the switch */ },
                )
            } else {
                WorkspaceShell(
                    container = container,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onSignedOut = { /* session flow drives the switch */ },
                    availableWidthDp = maxWidth.value.toInt(),
                )
            }
        }
    }
}

private const val ZOOM_STEP = 0.1f
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 2.0f
