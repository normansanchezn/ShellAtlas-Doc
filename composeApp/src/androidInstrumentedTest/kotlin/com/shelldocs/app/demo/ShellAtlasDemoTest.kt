package com.shelldocs.app.demo

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.shelldocs.app.MainActivity
import com.shelldocs.core.common.testing.DemoTestTags
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ShellAtlasDemoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testName = TestName()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val artifacts = DemoArtifacts(instrumentation)
    private val demoPauseMs = InstrumentationRegistry.getArguments().getString("demoPauseMs")?.toLongOrNull() ?: 1_500L

    @Before
    fun setUp() {
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        artifacts.startFlowRecording(testName.methodName)
    }

    @After
    fun tearDown() {
        artifacts.captureRootSnapshot(composeRule, testName.methodName)
        artifacts.stopFlowRecording()
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    @Test
    fun demo_authAssistantAndDashboardWalkthrough() {
        signIn()
        waitForTag(DemoTestTags.AssistantScreen)
        pauseForRecording()

        askAssistantQuestion("How does the EoSB1 process work?")
        waitForText("ShellAtlas AI")
        pauseForRecording(2_000L)

        navigateTo("Dashboard")
        waitForTag(DemoTestTags.DashboardScreen)
        waitForText("Knowledge operations overview")
        pauseForRecording()
    }

    @Test
    fun demo_documentsBrowseHistoryAndBookmarkWalkthrough() {
        signIn()

        navigateTo("Documents")
        waitForTag(DemoTestTags.DocumentsScreen)
        openDocument("EoSB1 Process for America's App - Android")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsHistory).performClick()
        waitForText("v2")
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsHistory).performClick()
        pauseForRecording(750L)

        composeRule.onNodeWithTag(DemoTestTags.DocumentsBookmark).performClick()
        pauseForRecording()
    }

    @Test
    fun demo_documentsCreateWalkthrough() {
        signIn()

        navigateTo("Documents")
        waitForTag(DemoTestTags.DocumentsScreen)
        composeRule.onNodeWithTag(DemoTestTags.DocumentsNew).performClick()
        waitForTag(DemoTestTags.DocumentsNewTitle)
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsNewTitle).performTextInput("Demo Release Checklist")
        composeRule.onNodeWithTag(DemoTestTags.DocumentsNewMarkdown).performTextInput(
            "# Demo Release Checklist\n\n- Validate smoke tests\n- Confirm rollout notes\n- Notify stakeholders",
        )
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsCreate).performClick()
        waitForText("Demo Release Checklist")
        pauseForRecording()
    }

    @Test
    fun demo_documentsEditAndPublishWalkthrough() {
        signIn()

        navigateTo("Documents")
        waitForTag(DemoTestTags.DocumentsScreen)
        openDocument("Authentication Flow - Android")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsEdit).performClick()
        waitForTag(DemoTestTags.DocumentsEditorMarkdown)
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsEditorMarkdown)
            .performTextInput("\n\n## Demo note\n\nRecorded from instrumented flow.")
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsSaveDraft).performClick()
        waitForText("Draft saved", timeoutMillis = 25_000L)
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsPublish).performClick()
        waitForText("Authentication Flow - Android")
        pauseForRecording()
    }

    @Test
    fun demo_updatesScanAndFilterWalkthrough() {
        signIn()

        navigateTo("Notifications")
        waitForTag(DemoTestTags.UpdatesScreen)
        waitForText("Maintenance triage")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.UpdatesScan).performClick()
        pauseForRecording(2_000L)
        composeRule.onNodeWithTag(DemoTestTags.updatesRisk("Critical")).performClick()
        pauseForRecording()
    }

    @Test
    fun demo_sourcesSyncAndReconnectWalkthrough() {
        signIn()

        navigateTo("Sources")
        waitForTag(DemoTestTags.SourcesScreen)
        waitForText("Imported Sources")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.sourceSync("Confluence")).performClick()
        pauseForRecording(2_000L)
    }

    // ── Complete flows ──────────────────────────────────────────────────────

    @Test
    fun flow_loginComplete() {
        waitForTag(DemoTestTags.SignInRoot)
        composeRule.onNodeWithTag(DemoTestTags.SignInEmail).performTextInput("demo@shell.com")
        composeRule.onNodeWithTag(DemoTestTags.SignInPassword).performTextInput("demo-pass-123")
        pauseForRecording(500L)
        composeRule.onNodeWithTag(DemoTestTags.PasswordToggle).performClick()
        pauseForRecording(400L)
        composeRule.onNodeWithTag(DemoTestTags.PasswordToggle).performClick()
        composeRule.onNodeWithTag(DemoTestTags.SignInSubmit).performClick()
        waitForTag(DemoTestTags.WorkspaceRoot)
        waitForTag(DemoTestTags.AssistantScreen)
        pauseForRecording()
    }

    @Test
    fun flow_assistantComplete() {
        signIn()

        waitForTag(DemoTestTags.AssistantScreen)
        waitForText("ShellAtlas")
        pauseForRecording()

        askAssistantQuestion("What is the EoSB1 authentication flow?")
        waitForText("ShellAtlas AI")
        pauseForRecording(2_000L)

        askAssistantQuestion("How do tokens rotate silently?")
        waitForText("ShellAtlas AI")
        pauseForRecording(1_500L)

        askAssistantQuestion("Summarise the release process.")
        waitForText("ShellAtlas AI")
        pauseForRecording(2_000L)
    }

    @Test
    fun flow_documentsComplete() {
        signIn()

        navigateTo("Documents")
        waitForTag(DemoTestTags.DocumentsScreen)
        pauseForRecording()

        openDocument("Authentication Flow - Android")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsHistory).performClick()
        waitForText("v2")
        pauseForRecording(750L)
        composeRule.onNodeWithTag(DemoTestTags.DocumentsHistory).performClick()
        pauseForRecording(500L)

        composeRule.onNodeWithTag(DemoTestTags.DocumentsBookmark).performClick()
        pauseForRecording(500L)

        composeRule.onNodeWithTag(DemoTestTags.DocumentsEdit).performClick()
        waitForTag(DemoTestTags.DocumentsEditorMarkdown)
        composeRule.onNodeWithTag(DemoTestTags.DocumentsEditorMarkdown)
            .performTextInput("\n\n## End-to-end note\n\nVerified in flow test.")
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsSaveDraft).performClick()
        waitForText("Draft saved", timeoutMillis = 25_000L)
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.DocumentsPublish).performClick()
        waitForText("Authentication Flow - Android")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.DocumentsNew).performClick()
        waitForTag(DemoTestTags.DocumentsNewTitle)
        composeRule.onNodeWithTag(DemoTestTags.DocumentsNewTitle)
            .performTextInput("Flow Test Document")
        composeRule.onNodeWithTag(DemoTestTags.DocumentsNewMarkdown)
            .performTextInput("# Flow Test\n\nCreated during complete documents flow.")
        composeRule.onNodeWithTag(DemoTestTags.DocumentsCreate).performClick()
        waitForText("Flow Test Document")
        pauseForRecording()
    }

    @Test
    fun flow_updatesPendingComplete() {
        signIn()

        navigateTo("Notifications")
        waitForTag(DemoTestTags.UpdatesScreen)
        waitForText("Maintenance triage")
        pauseForRecording()

        composeRule.onNodeWithTag(DemoTestTags.UpdatesScan).performClick()
        pauseForRecording(2_000L)

        composeRule.onNodeWithTag(DemoTestTags.updatesRisk("Critical")).performClick()
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.updatesRisk("High")).performClick()
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.updatesRisk("Critical")).performClick()
        pauseForRecording()
    }

    @Test
    fun flow_dashboardComplete() {
        signIn()

        navigateTo("Dashboard")
        waitForTag(DemoTestTags.DashboardScreen)
        waitForText("Knowledge operations overview")
        pauseForRecording()

        waitForText("Documents")
        waitForText("Queries")
        pauseForRecording(1_500L)

        composeRule.onNodeWithTag(DemoTestTags.DashboardRefresh).performClick()
        pauseForRecording(2_000L)
    }

    @Test
    fun demo_portraitAuthAndDashboardWalkthrough() {
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        signIn()
        waitForTag(DemoTestTags.AssistantScreen)
        pauseForRecording()

        navigateTo("Dashboard")
        waitForTag(DemoTestTags.DashboardScreen)
        waitForText("Knowledge operations overview")
        pauseForRecording()
    }

    @Test
    fun demo_signInValidationAndErrorDialog() {
        waitForTag(DemoTestTags.SignInRoot)

        // Malformed email: submit stays disabled — sign-in screen must remain
        composeRule.onNodeWithTag(DemoTestTags.SignInEmail).performTextInput("not-an-email")
        composeRule.onNodeWithTag(DemoTestTags.SignInPassword).performTextInput("somepassword")
        composeRule.onNodeWithTag(DemoTestTags.SignInSubmit).performClick()
        composeRule.onNodeWithTag(DemoTestTags.SignInRoot).assertExists()

        // Clear + submit with empty fields: no crash, screen stays
        composeRule.onNodeWithTag(DemoTestTags.SignInEmail).performTextClearance()
        composeRule.onNodeWithTag(DemoTestTags.SignInPassword).performTextClearance()
        composeRule.onNodeWithTag(DemoTestTags.SignInSubmit).performClick()
        composeRule.onNodeWithTag(DemoTestTags.SignInRoot).assertExists()

        pauseForRecording()
    }

    @Test
    fun demo_settingsSectionsAndSignOutWalkthrough() {
        signIn()

        navigateTo("Settings")
        waitForTag(DemoTestTags.SettingsScreen)
        waitForText("Platform configuration and preferences")
        pauseForRecording()

        openSettingsSection("AI Assistant")
        waitForText("AI Assistant")
        pauseForRecording()

        openSettingsSection("Team & Access")
        waitForText("Team Members")
        pauseForRecording()

        openSettingsSection("Notifications")
        waitForText("Weekly digest")
        pauseForRecording()

        openSettingsSection("Integrations")
        waitForText("Connection status, sync runs and reconnection live in the Sources section of the sidebar.")
        pauseForRecording()

        openSettingsSection("General")
        composeRule.onNodeWithTag(DemoTestTags.SettingsSignOut).performClick()
        waitForTag(DemoTestTags.SignInRoot)
        pauseForRecording()
    }

    // ── Narrow-width layout regressions ─────────────────────────────────────
    // These run in portrait (the narrow-width case) where wrapping/overflow
    // bugs in chip rows and headers actually reproduce; the landscape tests
    // above don't have enough width to catch them.

    @Test
    fun settings_portraitSectionChipsAllReachable() {
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        signIn()

        navigateTo("Settings")
        waitForTag(DemoTestTags.SettingsScreen)

        // Regression: the section chip row used to have no horizontal scroll,
        // so on narrow widths the trailing chip's label wrapped into several
        // vertical lines instead of staying on one line — every chip must
        // still be tappable and lead to its section's content.
        openSettingsSection("AI Assistant")
        waitForText("AI Assistant")
        openSettingsSection("Team & Access")
        waitForText("Team Members")
        openSettingsSection("Notifications")
        waitForText("Weekly digest")
        openSettingsSection("Integrations")
        waitForText("Connection status, sync runs and reconnection live in the Sources section of the sidebar.")
        openSettingsSection("General")
        waitForText("Dark mode")
    }

    @Test
    fun documents_portraitEditorHeaderStaysUsableWithLongTitle() {
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        signIn()

        navigateTo("Documents")
        waitForTag(DemoTestTags.DocumentsScreen)
        openDocument("Authentication Flow - Android")

        composeRule.onNodeWithTag(DemoTestTags.DocumentsEdit).performClick()
        waitForTag(DemoTestTags.DocumentsEditorMarkdown)

        // Regression: the editor header used to put the back button, the
        // doc title and the Save draft button all in one Row with no line
        // cap, so a long title wrapped across the header and pushed Save
        // draft out of reach. Both controls must stay reachable here.
        composeRule.onNodeWithTag(DemoTestTags.DocumentsEditorMarkdown)
            .performTextInput("\n\nPortrait regression check.")
        composeRule.onNodeWithTag(DemoTestTags.DocumentsSaveDraft).performClick()
        waitForText("Draft saved", timeoutMillis = 25_000L)
    }

    private fun signIn() {
        waitForTag(DemoTestTags.SignInRoot)
        composeRule.onNodeWithTag(DemoTestTags.SignInEmail).performTextInput("demo@shell.com")
        composeRule.onNodeWithTag(DemoTestTags.SignInPassword).performTextInput("demo-pass-123")
        pauseForRecording(500L)
        composeRule.onNodeWithTag(DemoTestTags.PasswordToggle).performClick()
        pauseForRecording(500L)
        composeRule.onNodeWithTag(DemoTestTags.PasswordToggle).performClick()
        composeRule.onNodeWithTag(DemoTestTags.SignInSubmit).performClick()
        waitForTag(DemoTestTags.WorkspaceRoot)
    }

    private fun askAssistantQuestion(question: String) {
        val input = composeRule.onNodeWithTag(DemoTestTags.AssistantInput)
        input.performTextClearance()
        input.performTextInput(question)
        pauseForRecording(500L)
        composeRule.onNodeWithTag(DemoTestTags.AssistantSend).performClick()
    }

    private fun navigateTo(routeTitle: String) {
        waitForTag(DemoTestTags.navRoute(routeTitle))
        composeRule.onNodeWithTag(DemoTestTags.navRoute(routeTitle)).performClick()
    }

    private fun openDocument(title: String) {
        waitForText(title)
        composeRule.onNodeWithText(title).performClick()
    }

    private fun openSettingsSection(sectionTitle: String) {
        val tag = DemoTestTags.settingsSection(sectionTitle)
        waitForTag(tag)
        composeRule.onNodeWithTag(tag).performClick()
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 15_000L) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().first()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 20_000L) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pauseForRecording(overrideMs: Long? = null) {
        Thread.sleep(overrideMs ?: demoPauseMs)
    }
}
