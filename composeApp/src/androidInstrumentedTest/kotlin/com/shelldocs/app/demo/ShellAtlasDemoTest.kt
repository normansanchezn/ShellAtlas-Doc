package com.shelldocs.app.demo

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.shelldocs.app.MainActivity
import com.shelldocs.core.common.testing.DemoTestTags
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ShellAtlasDemoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val demoPauseMs = InstrumentationRegistry.getArguments().getString("demoPauseMs")?.toLongOrNull() ?: 1_500L

    @Before
    fun setUp() {
        device.setOrientationLeft()
    }

    @After
    fun tearDown() {
        device.unfreezeRotation()
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
        composeRule.onNodeWithContentDescription("Close history").performClick()
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

        navigateTo("Updates Pending")
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
        composeRule.onNodeWithTag(DemoTestTags.sourceReconnect("Jira")).performClick()
        pauseForRecording()
        composeRule.onNodeWithTag(DemoTestTags.sourceSync("Jira")).performClick()
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
