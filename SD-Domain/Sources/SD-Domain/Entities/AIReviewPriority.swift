import Foundation

public enum AIReviewPriority: String, CaseIterable, Sendable, Codable, Comparable {
    case low
    case medium
    case high
    case critical

    public var displayName: String {
        switch self {
        case .low: "Low"
        case .medium: "Medium"
        case .high: "High"
        case .critical: "Critical"
        }
    }

    private var order: Int {
        switch self {
        case .low: 0
        case .medium: 1
        case .high: 2
        case .critical: 3
        }
    }

    public static func < (lhs: AIReviewPriority, rhs: AIReviewPriority) -> Bool {
        lhs.order < rhs.order
    }
}
