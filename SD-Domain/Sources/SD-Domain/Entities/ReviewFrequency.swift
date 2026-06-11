import Foundation

public enum ReviewFrequency: String, CaseIterable, Sendable, Codable {
    case monthly
    case quarterly
    case semiannual
    case annual

    public var displayName: String {
        switch self {
        case .monthly: "Monthly"
        case .quarterly: "Quarterly"
        case .semiannual: "Semi-Annual"
        case .annual: "Annual"
        }
    }

    public var days: Int {
        switch self {
        case .monthly: 30
        case .quarterly: 90
        case .semiannual: 180
        case .annual: 365
        }
    }
}
