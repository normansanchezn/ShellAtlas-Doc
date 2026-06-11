import Foundation
import DS_Core

enum ShellDocRemoteLogger {
    static func log(_ event: String, fields: [String: Any?] = [:]) {
        let prettyJSON = ShellDocEventLogStore.prettyJSONString(
            scope: "shelldoc.remote",
            event: event,
            fields: fields
        )
        Task { @MainActor in
            ShellDocEventLogStore.shared.record(
                prettyJSON: prettyJSON,
                scope: "shelldoc.remote",
                event: event,
                consoleLabel: "SHELLDOC REMOTE LOG"
            )
        }
    }

    static func redactedHeaders(from request: URLRequest) -> [String: String] {
        var headers = request.allHTTPHeaderFields ?? [:]
        if headers["Authorization"] != nil {
            headers["Authorization"] = "Bearer <redacted>"
        }
        return headers
    }

    static func responsePreview(from data: Data, limit: Int = 6000) -> String {
        ShellDocEventLogStore.responsePreview(from: data, limit: limit)
    }
}
