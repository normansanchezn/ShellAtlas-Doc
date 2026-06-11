import Foundation
import DS_Core

enum ShellDocOllamaLogger {
    static func log(_ event: String, fields: [String: Any?] = [:]) {
        let prettyJSON = ShellDocEventLogStore.prettyJSONString(
            scope: "shelldoc.ollama",
            event: event,
            fields: fields
        )
        Task { @MainActor in
            ShellDocEventLogStore.shared.record(
                prettyJSON: prettyJSON,
                scope: "shelldoc.ollama",
                event: event,
                consoleLabel: "SHELLDOC OLLAMA LOG"
            )
        }
    }

    static func responsePreview(from data: Data, limit: Int = 6000) -> String {
        ShellDocEventLogStore.responsePreview(from: data, limit: limit)
    }
}
