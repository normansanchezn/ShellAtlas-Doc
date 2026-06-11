import Foundation

public enum DocumentType: String, CaseIterable, Sendable, Codable {
    case process
    case architecture
    case guide
    case runbook
    case decision
    case reference

    public var displayName: String {
        switch self {
        case .process: "Process"
        case .architecture: "Architecture"
        case .guide: "Guide"
        case .runbook: "Runbook"
        case .decision: "Decision"
        case .reference: "Reference"
        }
    }
}
