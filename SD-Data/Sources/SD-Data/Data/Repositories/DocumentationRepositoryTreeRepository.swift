import Foundation
import SD_Domain

public struct DocumentationRepositoryTreeRepository: DocumentationTreeRepository {
    private let documentationRepository: any DocumentationRepository

    public init(documentationRepository: any DocumentationRepository) {
        self.documentationRepository = documentationRepository
    }

    public func getDocumentationTree() async throws -> [DocumentationNode] {
        let documents = try await documentationRepository.getDocuments()
        return DocumentationTreeBuilder.build(from: documents)
    }
}

enum DocumentationTreeBuilder {
    static func build(from documents: [DocumentationDocument]) -> [DocumentationNode] {
        let grouped = Dictionary(grouping: documents) { document in
            document.attributes.parentFolderId ?? normalizedFolderId(for: document)
        }

        let folders = grouped
            .map { folderId, documents in
                DocumentationNode(
                    id: folderId,
                    title: title(for: folderId, documents: documents),
                    type: .folder,
                    children: documents
                        .sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
                        .map(documentNode)
                )
            }
            .sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }

        return folders.isEmpty ? [] : [
            DocumentationNode(
                id: "shell-app-documentation",
                title: "Shell App Documentation",
                type: .folder,
                children: folders
            )
        ]
    }

    private static func normalizedFolderId(for document: DocumentationDocument) -> String {
        let platform = document.attributes.tags.first { ["android", "ios", "backend", "process"].contains($0.localizedLowercase) }
            ?? document.attributes.module
        return platform
            .localizedLowercase
            .replacingOccurrences(of: " ", with: "-")
            .replacingOccurrences(of: "/", with: "-")
    }

    private static func title(for folderId: String, documents: [DocumentationDocument]) -> String {
        if let first = documents.first?.attributes.module, !first.isEmpty {
            return first
        }
        return folderId
            .split(separator: "-")
            .map { $0.capitalized }
            .joined(separator: " ")
    }

    private static func documentNode(_ document: DocumentationDocument) -> DocumentationNode {
        DocumentationNode(
            id: "node-\(document.id)",
            title: document.title,
            type: .document,
            documentId: document.id
        )
    }
}
