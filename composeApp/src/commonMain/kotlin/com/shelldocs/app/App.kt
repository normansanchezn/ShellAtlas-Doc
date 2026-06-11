package com.shelldocs.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    ShellDocsTheme(darkTheme = isDarkTheme) {
        val session by container.authRepository.session.collectAsState()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (session == null) {
                val authViewModel = remember(container) { container.authViewModel() }
                DisposableEffect(authViewModel) { onDispose(authViewModel::clear) }
                SignInScreen(
                    viewModel = authViewModel,
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
