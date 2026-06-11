import Foundation

public enum Platform: String, CaseIterable, Sendable, Codable {
    case android
    case iOS
    case web
    case crossPlatform = "cross-platform"
    case backend

    public var displayName: String {
        switch self {
        case .android: "Android"
        case .iOS: "iOS"
        case .web: "Web"
        case .crossPlatform: "Cross-Platform"
        case .backend: "Backend"
        }
    }
}
