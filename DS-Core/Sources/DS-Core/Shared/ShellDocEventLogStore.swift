import Foundation
import Observation

@MainActor
@Observable
public final class ShellDocEventLogStore {
    public static let shared = ShellDocEventLogStore()

    public private(set) var entries: [ShellDocLogEntry] = []
    public var maximumEntries: Int = 200

    public init() {}

    public func record(
        scope: String,
        event: String,
        fields: [String: Any?] = [:],
        consoleLabel: String
    ) {
        record(
            prettyJSON: Self.prettyJSONString(
                scope: scope,
                event: event,
                fields: fields
            ),
            scope: scope,
            event: event,
            consoleLabel: consoleLabel
        )
    }

    public func record(
        prettyJSON: String,
        scope: String,
        event: String,
        consoleLabel: String
    ) {
        entries.append(
            ShellDocLogEntry(
                scope: scope,
                event: event,
                timestamp: Date(),
                prettyJSON: prettyJSON
            )
        )

        if entries.count > maximumEntries {
            entries.removeFirst(entries.count - maximumEntries)
        }

        print(
            """

            --- \(consoleLabel) ---
            \(prettyJSON)
            --- END \(consoleLabel) ---

            """
        )
    }

    public func clear() {
        entries.removeAll()
    }

    nonisolated public static func prettyJSONString(
        scope: String,
        event: String,
        fields: [String: Any?] = [:]
    ) -> String {
        let payload = makePayload(
            scope: scope,
            event: event,
            fields: fields
        )

        guard JSONSerialization.isValidJSONObject(payload),
              let data = try? JSONSerialization.data(
                withJSONObject: payload,
                options: [.prettyPrinted, .sortedKeys]
              ),
              let json = String(data: data, encoding: .utf8)
        else {
            return "\(payload)"
        }

        return json
    }

    nonisolated public static func responsePreview(
        from data: Data,
        limit: Int = 6000
    ) -> String {
        let raw = String(data: data, encoding: .utf8)?
            .trimmingCharacters(in: .whitespacesAndNewlines)

        let preview = raw.flatMap { text -> String in
            if let jsonData = text.data(using: .utf8),
               let object = try? JSONSerialization.jsonObject(with: jsonData),
               JSONSerialization.isValidJSONObject(object),
               let pretty = try? JSONSerialization.data(
                    withJSONObject: object,
                    options: [.prettyPrinted, .sortedKeys]
               ),
               let prettyString = String(data: pretty, encoding: .utf8) {
                return prettyString
            }

            return text
        } ?? data.base64EncodedString()

        guard preview.count > limit else {
            return preview
        }

        let index = preview.index(
            preview.startIndex,
            offsetBy: limit
        )

        return String(preview[..<index]) + "\n… [truncated]"
    }

    nonisolated private static func makePayload(
        scope: String,
        event: String,
        fields: [String: Any?]
    ) -> [String: Any] {
        var payload: [String: Any] = [
            "timestamp": ISO8601DateFormatter().string(from: Date()),
            "scope": scope,
            "event": event
        ]

        for (key, value) in fields {
            guard let value else { continue }
            payload[key] = sanitize(value)
        }

        return payload
    }

    nonisolated private static func sanitize(_ value: Any) -> Any {
        switch value {
        case let url as URL:
            return url.absoluteString

        case let date as Date:
            return ISO8601DateFormatter().string(from: date)

        case let data as Data:
            return data.base64EncodedString()

        case let dictionary as [String: Any]:
            var sanitized: [String: Any] = [:]

            for (key, value) in dictionary {
                sanitized[key] = sanitize(value)
            }

            return sanitized

        case let dictionary as [String: String]:
            return dictionary

        case let array as [Any]:
            return array.map { sanitize($0) }

        case let number as NSNumber:
            return number

        case let bool as Bool:
            return bool

        case let string as String:
            return string

        case Optional<Any>.none:
            return NSNull()

        default:
            return String(describing: value)
        }
    }
}

public struct ShellDocLogEntry: Identifiable, Sendable {
    public let id: UUID
    public let scope: String
    public let event: String
    public let timestamp: Date
    public let prettyJSON: String

    public init(
        id: UUID = UUID(),
        scope: String,
        event: String,
        timestamp: Date,
        prettyJSON: String
    ) {
        self.id = id
        self.scope = scope
        self.event = event
        self.timestamp = timestamp
        self.prettyJSON = prettyJSON
    }
}
