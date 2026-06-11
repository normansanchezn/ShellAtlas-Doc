import Foundation

public enum TicketStatus: String, CaseIterable, Sendable, Codable {
    case open
    case inProgress = "in_progress"
    case closed
    case resolved

    public var displayName: String {
        switch self {
        case .open: "Open"
        case .inProgress: "In Progress"
        case .closed: "Closed"
        case .resolved: "Resolved"
        }
    }
}
