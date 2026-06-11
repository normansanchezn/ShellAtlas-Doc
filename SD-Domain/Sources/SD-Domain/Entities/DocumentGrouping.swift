import Foundation

public enum DocumentGrouping: String, CaseIterable, Sendable {
    case type     = "Type"
    case platform = "Platform"
    case owner    = "Owner"
    case status   = "Status"

    public var systemImage: String {
        switch self {
        case .type:     "doc.fill"
        case .platform: "iphone"
        case .owner:    "person.fill"
        case .status:   "circle.fill"
        }
    }
}
