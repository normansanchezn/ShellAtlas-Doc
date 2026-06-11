import Foundation
import DS_Core

enum ShellDocConnectionLogger {
    static func log(_ event: String, fields: [String: Any?] = [:]) {
        let prettyJSON = ShellDocEventLogStore.prettyJSONString(
            scope: "shelldoc.connection",
            event: event,
            fields: fields
        )
        Task { @MainActor in
            ShellDocEventLogStore.shared.record(
                prettyJSON: prettyJSON,
                scope: "shelldoc.connection",
                event: event,
                consoleLabel: "SHELLDOC CONNECTION LOG"
            )
        }
    }
}
