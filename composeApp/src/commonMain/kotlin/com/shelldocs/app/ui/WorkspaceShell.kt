package com.shelldocs.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.app.di.AppContainer
import com.shelldocs.app.navigation.AppRoute
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.feature.assistant.ui.AssistantScreen
import com.shelldocs.feature.dashboard.ui.DashboardScreen
import com.shelldocs.feature.documents.ui.DocumentsScreen
import com.shelldocs.feature.settings.ui.SettingsScreen
import com.shelldocs.feature.sources.ui.SourcesScreen
import com.shelldocs.feature.updates.ui.UpdatesScreen

/**
 * Authenticated workspace with three layouts: above [WIDE_LAYOUT_MIN_WIDTH_DP]
 * the full desktop shell renders (sidebar + content); between
 * [RAIL_LAYOUT_MIN_WIDTH_DP] and that, a compact icon rail for tablets;
 * below [RAIL_LAYOUT_MIN_WIDTH_DP] the layout collapses to content + bottom
 * navigation for phones.
 */
@Composable
fun WorkspaceShell(
    container: AppContainer,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignedOut: () -> Unit,
    availableWidthDp: Int,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val route by container.navigator.route.collectAsState()
    val session by container.authRepository.session.collectAsState()
    val isWide = availableWidthDp >= WIDE_LAYOUT_MIN_WIDTH_DP
    val isRail = !isWide && availableWidthDp >= RAIL_LAYOUT_MIN_WIDTH_DP
    // Content next to the rail counts as "wide" once it has room for a
    // multi-column layout on its own (rail takes 72dp off the width).
    val isRailContentWide = isRail && (availableWidthDp - 72) >= CONTENT_WIDE_MIN_WIDTH_DP
    var searchQuery by remember { mutableStateOf("") }

    when {
        isWide -> {
            Box(modifier = modifier.fillMaxSize().background(colors.background)) {
                ShellWorkspaceBackground(Modifier.fillMaxSize())
                Row(modifier = Modifier.fillMaxSize()) {
                    WorkspaceSidebar(
                        activeRoute = route,
                        pendingUpdatesCount = PENDING_BADGE_PLACEHOLDER,
                        user = session?.user,
                        isDarkTheme = isDarkTheme,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onNavigate = container.navigator::navigate,
                        onToggleTheme = onToggleTheme,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.border),
                    )
                    RouteContent(
                        container = container,
                        route = route,
                        isWide = true,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onSignedOut = onSignedOut,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        isRail -> {
            Box(modifier = modifier.fillMaxSize().background(colors.background)) {
                ShellWorkspaceBackground(Modifier.fillMaxSize())
                Row(modifier = Modifier.fillMaxSize()) {
                    WorkspaceRail(
                        activeRoute = route,
                        pendingUpdatesCount = PENDING_BADGE_PLACEHOLDER,
                        isDarkTheme = isDarkTheme,
                        onNavigate = container.navigator::navigate,
                        onToggleTheme = onToggleTheme,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.border),
                    )
                    RouteContent(
                        container = container,
                        route = route,
                        isWide = isRailContentWide,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onSignedOut = onSignedOut,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        else -> {
            Box(modifier = modifier.fillMaxSize().background(colors.background)) {
                ShellWorkspaceBackground(Modifier.fillMaxSize())
                Column(modifier = Modifier.fillMaxSize()) {
                    RouteContent(
                        container = container,
                        route = route,
                        isWide = false,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onSignedOut = onSignedOut,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                    WorkspaceBottomBar(activeRoute = route, onNavigate = container.navigator::navigate)
                }
            }
        }
    }
}

@Composable
private fun RouteContent(
    container: AppContainer,
    route: AppRoute,
    isWide: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (route) {
        AppRoute.ASSISTANT -> {
            val viewModel = remember(container) { container.assistantViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            AssistantScreen(
                viewModel = viewModel,
                isWide = isWide,
                onOpenDocument = container.navigator::openDocument,
                modifier = modifier.testTag(DemoTestTags.AssistantScreen),
            )
        }
        AppRoute.DOCUMENTS -> {
            val viewModel = remember(container) { container.documentsViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            DocumentsScreen(
                viewModel = viewModel,
                isWide = isWide,
                modifier = modifier.testTag(DemoTestTags.DocumentsScreen),
            )
        }
        AppRoute.UPDATES -> {
            val viewModel = remember(container) { container.updatesViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            UpdatesScreen(
                viewModel = viewModel,
                isWide = isWide,
                modifier = modifier.testTag(DemoTestTags.UpdatesScreen),
            )
        }
        AppRoute.DASHBOARD -> {
            val viewModel = remember(container) { container.dashboardViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            DashboardScreen(
                viewModel = viewModel,
                isWide = isWide,
                modifier = modifier.testTag(DemoTestTags.DashboardScreen),
            )
        }
        AppRoute.SOURCES -> {
            val viewModel = remember(container) { container.sourcesViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            SourcesScreen(
                viewModel = viewModel,
                isWide = isWide,
                modifier = modifier.testTag(DemoTestTags.SourcesScreen),
            )
        }
        AppRoute.SETTINGS -> {
            val viewModel = remember(container) { container.settingsViewModel() }
            DisposableEffect(viewModel) { onDispose(viewModel::clear) }
            SettingsScreen(
                viewModel = viewModel,
                isWide = isWide,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onSignedOut = onSignedOut,
                modifier = modifier.testTag(DemoTestTags.SettingsScreen),
            )
        }
    }
}

const val WIDE_LAYOUT_MIN_WIDTH_DP = 840

/** Below this, phones get the bottom bar; at/above it, tablets get the icon rail. */
const val RAIL_LAYOUT_MIN_WIDTH_DP = 600

/** Minimum content width (next to the rail) for screens to use multi-column layouts. */
private const val CONTENT_WIDE_MIN_WIDTH_DP = 700

private const val PENDING_BADGE_PLACEHOLDER = 12
