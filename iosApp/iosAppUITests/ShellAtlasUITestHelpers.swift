import XCTest

// MARK: - Accessibility identifier constants matching DemoTestTags.kt

enum Tag {
    static let signInRoot       = "demo_sign_in_root"
    static let signInEmail      = "demo_sign_in_email"
    static let signInPassword   = "demo_sign_in_password"
    static let signInSubmit     = "demo_sign_in_submit"
    static let passwordToggle   = "demo_password_toggle"
    static let workspaceRoot    = "demo_workspace_root"

    static let assistantScreen  = "demo_assistant_screen"
    static let assistantInput   = "demo_assistant_input"
    static let assistantSend    = "demo_assistant_send"

    static let documentsScreen  = "demo_documents_screen"
    static let documentsNew     = "demo_documents_new"
    static let documentsEdit    = "demo_documents_edit"
    static let documentsHistory = "demo_documents_history"
    static let documentsBookmark = "demo_documents_bookmark"

    static let dashboardScreen  = "demo_dashboard_screen"
    static let updatesScreen    = "demo_updates_screen"
    static let updatesScan      = "demo_updates_scan"
    static let sourcesScreen    = "demo_sources_screen"
    static let settingsScreen   = "demo_settings_screen"
    static let settingsSignOut  = "demo_settings_sign_out"

    static func navRoute(_ title: String) -> String {
        "demo_nav_" + title.lowercased().replacingOccurrences(of: " ", with: "_")
    }
    static func updatesRisk(_ name: String) -> String {
        "demo_updates_risk_" + name.lowercased()
    }
    static func sourceSync(_ kind: String) -> String {
        "demo_source_sync_" + kind.lowercased().replacingOccurrences(of: " ", with: "_")
    }
    static func sourceReconnect(_ kind: String) -> String {
        "demo_source_reconnect_" + kind.lowercased().replacingOccurrences(of: " ", with: "_")
    }
    static func settingsSection(_ name: String) -> String {
        "demo_settings_section_" + name
            .lowercased()
            .replacingOccurrences(of: " ", with: "_")
            .replacingOccurrences(of: "&", with: "_")
    }
}

// MARK: - XCTestCase helpers for Compose-on-iOS accessibility

extension XCTestCase {

    /// Returns the first descendant matching the given accessibility identifier,
    /// waiting up to `timeout` seconds.
    func element(_ id: String, in app: XCUIApplication, timeout: TimeInterval = 20) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }

    func waitForElement(_ id: String, in app: XCUIApplication, timeout: TimeInterval = 20) {
        let el = element(id, in: app, timeout: timeout)
        XCTAssertTrue(el.waitForExistence(timeout: timeout), "Element '\(id)' did not appear within \(timeout)s")
    }

    func tapElement(_ id: String, in app: XCUIApplication, timeout: TimeInterval = 15) {
        let el = element(id, in: app)
        XCTAssertTrue(el.waitForExistence(timeout: timeout), "Tap target '\(id)' not found")
        el.tap()
    }

    func typeInto(_ id: String, text: String, in app: XCUIApplication, timeout: TimeInterval = 15) {
        let el = element(id, in: app)
        XCTAssertTrue(el.waitForExistence(timeout: timeout), "Input '\(id)' not found")
        el.tap()
        el.typeText(text)
    }

    // MARK: - Reusable sign-in flow

    func signIn(app: XCUIApplication, email: String = "demo@shell.com", password: String = "demo-pass-123") {
        waitForElement(Tag.signInRoot, in: app)
        typeInto(Tag.signInEmail, text: email, in: app)
        typeInto(Tag.signInPassword, text: password, in: app)
        tapElement(Tag.signInSubmit, in: app)
        waitForElement(Tag.workspaceRoot, in: app)
    }

    func navigateTo(_ routeTitle: String, in app: XCUIApplication) {
        tapElement(Tag.navRoute(routeTitle), in: app)
    }
}
