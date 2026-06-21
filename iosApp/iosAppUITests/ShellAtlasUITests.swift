import XCTest

// MARK: - ShellAtlas iOS Instrumented Flow Tests
//
// Mirror of the Android ShellAtlasDemoTest suite.  Each test exercises one
// complete user journey through the Compose-Multiplatform app running on the
// iOS Simulator.
//
// Compose Multiplatform maps Modifier.testTag() to UIAccessibility
// accessibilityIdentifier, so every element is located by its DemoTestTags
// constant (see ShellAtlasUITestHelpers.swift).
//
// HOW TO RUN
// ----------
//   1. Open iosApp.xcodeproj in Xcode.
//   2. Select the "iosAppUITests" scheme (or add this target via
//      File > New > Target > UI Testing Bundle if not yet present).
//   3. Product > Test  (⌘U) or run individual tests via the gutter icon.

final class ShellAtlasUITests: XCTestCase {

    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["-demo_pause_ms", "0"]
        app.launch()
    }

    override func tearDownWithError() throws {
        app = nil
    }

    // MARK: - Auth

    func test_signIn_success() {
        signIn(app: app)
        XCTAssertTrue(element(Tag.workspaceRoot, in: app).exists)
    }

    func test_signIn_togglePasswordVisibility() {
        waitForElement(Tag.signInRoot, in: app)
        typeInto(Tag.signInEmail, text: "demo@shell.com", in: app)
        typeInto(Tag.signInPassword, text: "demo-pass-123", in: app)
        tapElement(Tag.passwordToggle, in: app)
        tapElement(Tag.passwordToggle, in: app)
        tapElement(Tag.signInSubmit, in: app)
        waitForElement(Tag.workspaceRoot, in: app)
    }

    func test_signIn_badEmail_disablesSubmit() {
        waitForElement(Tag.signInRoot, in: app)
        typeInto(Tag.signInEmail, text: "not-an-email", in: app)
        typeInto(Tag.signInPassword, text: "somepassword", in: app)
        let submitBtn = element(Tag.signInSubmit, in: app)
        XCTAssertTrue(submitBtn.waitForExistence(timeout: 5))
        // Compose disables the button; submit tap must NOT navigate away
        submitBtn.tap()
        let signInStillVisible = element(Tag.signInRoot, in: app).waitForExistence(timeout: 4)
        XCTAssertTrue(signInStillVisible, "Sign-in screen should remain after bad email")
    }

    func test_signIn_emptyFields_disablesSubmit() {
        waitForElement(Tag.signInRoot, in: app)
        let submitBtn = element(Tag.signInSubmit, in: app)
        XCTAssertTrue(submitBtn.waitForExistence(timeout: 5))
        submitBtn.tap()
        XCTAssertTrue(element(Tag.signInRoot, in: app).exists, "Screen should stay on empty submit")
    }

    // MARK: - Assistant + Dashboard

    func test_assistantAndDashboardWalkthrough() {
        signIn(app: app)
        waitForElement(Tag.assistantScreen, in: app)

        typeInto(Tag.assistantInput, text: "How does the EoSB1 process work?", in: app)
        tapElement(Tag.assistantSend, in: app)
        // Wait for AI response header
        let aiHeader = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'ShellAtlas AI'"))
            .firstMatch
        XCTAssertTrue(aiHeader.waitForExistence(timeout: 30))

        navigateTo("Dashboard", in: app)
        waitForElement(Tag.dashboardScreen, in: app)
    }

    // MARK: - Documents

    func test_documentsBrowseHistoryAndBookmark() {
        signIn(app: app)
        navigateTo("Documents", in: app)
        waitForElement(Tag.documentsScreen, in: app)

        let docTitle = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'EoSB1 Process'"))
            .firstMatch
        XCTAssertTrue(docTitle.waitForExistence(timeout: 15))
        docTitle.tap()

        tapElement(Tag.documentsHistory, in: app)
        let v2 = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'v2'"))
            .firstMatch
        XCTAssertTrue(v2.waitForExistence(timeout: 10))
        tapElement(Tag.documentsHistory, in: app)
        tapElement(Tag.documentsBookmark, in: app)
    }

    func test_documentsCreate() {
        signIn(app: app)
        navigateTo("Documents", in: app)
        waitForElement(Tag.documentsScreen, in: app)

        tapElement(Tag.documentsNew, in: app)
        waitForElement("demo_documents_new_title", in: app)

        typeInto("demo_documents_new_title", text: "iOS Demo Checklist", in: app)
        typeInto("demo_documents_new_markdown", text: "# iOS Demo Checklist\n\n- Validate smoke tests", in: app)
        tapElement("demo_documents_create", in: app)

        let created = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'iOS Demo Checklist'"))
            .firstMatch
        XCTAssertTrue(created.waitForExistence(timeout: 15))
    }

    func test_documentsEditAndPublish() {
        signIn(app: app)
        navigateTo("Documents", in: app)
        waitForElement(Tag.documentsScreen, in: app)

        let doc = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'Authentication Flow'"))
            .firstMatch
        XCTAssertTrue(doc.waitForExistence(timeout: 15))
        doc.tap()

        tapElement(Tag.documentsEdit, in: app)
        waitForElement("demo_documents_editor_markdown", in: app)

        typeInto("demo_documents_editor_markdown",
                 text: "\n\n## iOS note\n\nRecorded from XCUITest.",
                 in: app)
        tapElement("demo_documents_save_draft", in: app)

        let draftSaved = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'Draft saved'"))
            .firstMatch
        XCTAssertTrue(draftSaved.waitForExistence(timeout: 25))
        tapElement("demo_documents_publish", in: app)
    }

    // MARK: - Updates

    func test_updatesScanAndFilter() {
        signIn(app: app)
        navigateTo("Notifications", in: app)
        waitForElement(Tag.updatesScreen, in: app)

        tapElement(Tag.updatesScan, in: app)
        tapElement(Tag.updatesRisk("Critical"), in: app)
    }

    // MARK: - Sources

    func test_sourcesSyncAndReconnect() {
        signIn(app: app)
        navigateTo("Sources", in: app)
        waitForElement(Tag.sourcesScreen, in: app)

        tapElement(Tag.sourceSync("Confluence"), in: app)
        tapElement(Tag.sourceReconnect("Jira"), in: app)
        tapElement(Tag.sourceSync("Jira"), in: app)
    }

    // MARK: - Settings + Sign Out

    func test_settingsAndSignOut() {
        signIn(app: app)
        navigateTo("Settings", in: app)
        waitForElement(Tag.settingsScreen, in: app)

        for section in ["AI Assistant", "Team _ Access", "Notifications", "Integrations", "General"] {
            tapElement(Tag.settingsSection(section), in: app)
        }

        tapElement(Tag.settingsSignOut, in: app)
        waitForElement(Tag.signInRoot, in: app)
    }
}
