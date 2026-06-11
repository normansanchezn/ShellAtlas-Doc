import Foundation

public enum DocumentationEditorMode: Hashable, Identifiable, Sendable {
    case create(parentFolderId: String?)
    case edit(documentId: String)

    public var id: String {
        switch self {
        case .create(let parentFolderId): "create-\(parentFolderId ?? "default")"
        case .edit(let documentId): "edit-\(documentId)"
        }
    }

    var title: String {
        switch self {
        case .create: "New Document"
        case .edit: "Edit Document"
        }
    }

    var parentFolderId: String? {
        guard case .create(let parentFolderId) = self else { return nil }
        return parentFolderId
    }
}
