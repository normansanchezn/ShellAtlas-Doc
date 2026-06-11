import Foundation

public enum TicketType: String, CaseIterable, Sendable, Codable {
    case bug
    case feature
    case task
    case epic
    case story

    public var displayName: String {
        switch self {
        case .bug: "Bug"
        case .feature: "Feature"
        case .task: "Task"
        case .epic: "Epic"
        case .story: "Story"
        }
    }
}
