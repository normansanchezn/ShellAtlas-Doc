import Foundation

public enum ConfidenceLevel: String, CaseIterable, Sendable, Codable, Comparable {
    case low
    case medium
    case high

    public var displayName: String {
        switch self {
        case .low: "Low"
        case .medium: "Medium"
        case .high: "High"
        }
    }

    private var order: Int {
        switch self {
        case .low: 0
        case .medium: 1
        case .high: 2
        }
    }

    public static func < (lhs: ConfidenceLevel, rhs: ConfidenceLevel) -> Bool {
        lhs.order < rhs.order
    }
}
