import Foundation

public enum DomainError: LocalizedError, Sendable {
    case jsonNotFound(String)
    case decodingFailed(String)
    case documentNotFound(String)
    case remoteRequestFailed(String)

    public var errorDescription: String? {
        switch self {
        case .jsonNotFound(let name): "Mock JSON file '\(name)' not found in bundle"
        case .decodingFailed(let detail): "Failed to decode mock data: \(detail)"
        case .documentNotFound(let id): "Document with id '\(id)' not found"
        case .remoteRequestFailed(let detail): "Remote request failed: \(detail)"
        }
    }
}
