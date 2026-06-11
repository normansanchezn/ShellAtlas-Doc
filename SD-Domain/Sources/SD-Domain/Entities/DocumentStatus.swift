import Foundation

public enum DocumentStatus: String, CaseIterable, Sendable, Codable {
    case active
    case review
    case outdated
    case archived
    case draft

    public var displayName: String {
        switch self {
        case .active: "Active"
        case .review: "Under Review"
        case .outdated: "Outdated"
        case .archived: "Archived"
        case .draft: "Draft"
        }
    }
}
