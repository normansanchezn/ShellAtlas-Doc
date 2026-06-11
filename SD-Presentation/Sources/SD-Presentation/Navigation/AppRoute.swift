import Foundation

enum AppRoute: Hashable {
    case assistant          // Home, Knowledge Assistant chat
    case explorer           // File-tree browser + Markdown reader
    case document(id: String) // Read a specific document (cross-feature navigation)
    case updatesPending     // Prioritized list of documents with pending updates
    case dashboard          // Aggregate health metrics
    case sources            // Mock data viewer
    case settings

    var title: String {
        switch self {
        case .assistant:    "Assistant"
        case .explorer:     "Docs"
        case .document:     "Document"
        case .updatesPending: "Updates Pending"
        case .dashboard:    "Dashboard"
        case .sources:      "Sources"
        case .settings:     "Settings"
        }
    }

    var systemImage: String {
        switch self {
        case .assistant:    "bubble.left.and.bubble.right.fill"
        case .explorer:     "folder.fill"
        case .document:     "doc.text.fill"
        case .updatesPending: "exclamationmark.triangle.fill"
        case .dashboard:    "chart.bar.fill"
        case .sources:      "cylinder.split.1x2.fill"
        case .settings:     "gearshape.fill"
        }
    }
}
