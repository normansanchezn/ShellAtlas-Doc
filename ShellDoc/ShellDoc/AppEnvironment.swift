import Foundation
import SD_Domain

struct AppEnvironment: Sendable {
    let isMockMode: Bool
    let appVersion: String
    let usesOllamaAssistant: Bool
    let ollamaBaseURL: URL
    let ollamaModel: String
    let shellDocAPIBaseURL: URL?
    let shellDocAPIBearerToken: String?

    static var current: AppEnvironment {
        let environment = ProcessInfo.processInfo.environment
        let apiBaseURLString = environment["SHELLDOC_API_BASE_URL"].sanitizedEnvironmentValue
        let ollamaBaseURLString = environment["SHELLDOC_OLLAMA_BASE_URL"].sanitizedEnvironmentValue
        let apiBaseURL = apiBaseURLString.flatMap(URL.init(string:))
        let ollamaBaseURL = URL(
            string: ollamaBaseURLString ?? DocumentationAssistantConfiguration.defaultOllamaBaseURL
        ) ?? URL(string: DocumentationAssistantConfiguration.defaultOllamaBaseURL)!
        let ollamaEnabled = Self.parseBoolean(environment["SHELLDOC_USE_OLLAMA_ASSISTANT"]) ?? (ollamaBaseURLString != nil)
        return AppEnvironment(
            isMockMode: apiBaseURL == nil,
            appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0-mvp",
            usesOllamaAssistant: ollamaEnabled,
            ollamaBaseURL: ollamaBaseURL,
            ollamaModel: environment["SHELLDOC_OLLAMA_MODEL"].sanitizedEnvironmentValue ?? DocumentationAssistantConfiguration.defaultOllamaModel,
            shellDocAPIBaseURL: apiBaseURL,
            shellDocAPIBearerToken: environment["SHELLDOC_API_BEARER_TOKEN"].sanitizedEnvironmentValue
        )
    }

    static let mock = AppEnvironment(
        isMockMode: true,
        appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0-mvp",
        usesOllamaAssistant: false,
        ollamaBaseURL: URL(string: DocumentationAssistantConfiguration.defaultOllamaBaseURL)!,
        ollamaModel: DocumentationAssistantConfiguration.defaultOllamaModel,
        shellDocAPIBaseURL: nil,
        shellDocAPIBearerToken: nil
    )

    private static func parseBoolean(_ value: String?) -> Bool? {
        guard let value else { return nil }
        switch value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() {
        case "1", "true", "yes", "y", "on":
            return true
        case "0", "false", "no", "n", "off":
            return false
        default:
            return nil
        }
    }
}

private extension Optional where Wrapped == String {
    var sanitizedEnvironmentValue: String? {
        guard let value = self?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else {
            return nil
        }
        return value.trimmingCharacters(in: CharacterSet(charactersIn: "\"'"))
    }
}
