import Foundation
import Observation
import SD_Domain

@Observable
@MainActor
final class DocumentationExplorerViewModel {
    var tree: [DocumentationNode] = []
    var expandedNodeIDs: Set<String> = []
    var selectedDocumentID: String?
    var selectedFolderID: String?
    var selectedDocument: DocumentationDocument?
    var searchQuery: String = ""
    var searchResults: [DocumentationDocument] = []
    var filters = DocumentationTreeFilters()
    var isLoadingTree = false
    var isLoadingDocument = false
    var errorMessage: String?
    var editorMode: DocumentationEditorMode?
    var renameTarget: DocumentationRenameTarget?
    var deleteTarget: DocumentationDeleteTarget?
    var renameText = ""

    var isSearching: Bool {
        !searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var visibleTree: [DocumentationNode] {
        filterTree(tree)
    }

    var moduleOptions: [String] {
        ["All"] + uniqueOptions(\.attributes.module)
    }

    var teamOptions: [String] {
        ["All"] + uniqueOptions(\.attributes.team)
    }

    var platformOptions: [String] {
        ["All", "Android", "iOS", "Backend", "Process"]
    }

    private var getTreeUseCase: (any GetDocumentationTreeUseCaseProtocol)?
    private var getDocumentsUseCase: (any GetDocumentationDocumentsUseCaseProtocol)?
    private var getDocumentUseCase: (any GetDocumentationDocumentDetailUseCaseProtocol)?
    private var searchUseCase: (any SearchDocumentsByTitleUseCaseProtocol)?
    private var createUseCase: (any CreateDocumentUseCaseProtocol)?
    private var updateUseCase: (any UpdateDocumentUseCaseProtocol)?
    private var deleteUseCase: (any DeleteDocumentUseCaseProtocol)?
    private var documentsByID: [String: DocumentationDocument] = [:]
    private var folderTitleOverrides: [String: String] = [:]
    private var deletedFolderIDs: Set<String> = []

    func configure(services: any AppServices) {
        getTreeUseCase = services.getDocumentationTreeUseCase
        getDocumentsUseCase = services.getDocumentationDocumentsUseCase
        getDocumentUseCase = services.getDocumentationDocumentDetailUseCase
        searchUseCase = services.searchDocumentsByTitleUseCase
        createUseCase = services.createDocumentUseCase
        updateUseCase = services.updateDocumentUseCase
        deleteUseCase = services.deleteDocumentUseCase
    }

    func onAppear() async {
        guard tree.isEmpty else { return }
        await refresh()
    }

    func refresh() async {
        await loadTree(selectFirstIfNeeded: selectedDocumentID == nil)
    }

    func toggleNode(_ node: DocumentationNode) {
        guard node.type == .folder else { return }
        selectedFolderID = node.id
        selectedDocumentID = nil
        if expandedNodeIDs.contains(node.id) {
            expandedNodeIDs.remove(node.id)
        } else {
            expandedNodeIDs.insert(node.id)
        }
    }

    func selectDocument(id: String) async {
        selectedDocumentID = id
        selectedFolderID = parentFolderID(forDocumentID: id, in: tree)
        isLoadingDocument = true
        errorMessage = nil
        defer { isLoadingDocument = false }

        do {
            selectedDocument = try await getDocumentUseCase?.execute(id: id)
        } catch {
            selectedDocument = nil
            errorMessage = error.localizedDescription
        }
    }

    func searchByTitle() async {
        guard let searchUseCase else { return }
        let query = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else {
            searchResults = []
            return
        }

        do {
            searchResults = try await searchUseCase.execute(query: query)
        } catch {
            searchResults = []
            errorMessage = error.localizedDescription
        }
    }

    func clearSearch() {
        searchQuery = ""
        searchResults = []
    }

    func clearFilters() {
        filters = DocumentationTreeFilters()
    }

    func createNewDocument(in parentFolderID: String? = nil) {
        editorMode = .create(parentFolderId: resolvedParentFolderID(explicitParentFolderID: parentFolderID))
    }

    func editSelectedDocument() {
        guard let selectedDocumentID else { return }
        editorMode = .edit(documentId: selectedDocumentID)
    }

    func effectiveStatus(for document: DocumentationDocument) -> DocumentationStatus {
        DocumentationReviewPolicy.effectiveStatus(for: document)
    }

    func updateReason(for document: DocumentationDocument) -> String? {
        DocumentationReviewPolicy.updateReason(for: document)
    }

    func handleEditorSave(documentID: String) async {
        editorMode = nil
        selectedDocumentID = documentID
        await loadTree(selectFirstIfNeeded: false)
        await selectDocument(id: documentID)
    }

    func displayTitle(for node: DocumentationNode) -> String {
        folderTitleOverrides[node.id] ?? node.title
    }

    func requestRenameFolder(_ node: DocumentationNode) {
        renameTarget = .folder(id: node.id)
        renameText = displayTitle(for: node)
    }

    func requestRenameDocument(_ node: DocumentationNode) {
        guard let documentID = node.documentId else { return }
        renameTarget = .document(id: documentID)
        renameText = documentsByID[documentID]?.title ?? node.title
    }

    func commitRename() async {
        let newTitle = renameText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !newTitle.isEmpty, let renameTarget else { return }

        do {
            switch renameTarget {
            case .folder(let id):
                folderTitleOverrides[id] = newTitle
            case .document(let id):
                guard var document = documentsByID[id] else { return }
                document.title = newTitle
                document.attributes.lastUpdated = Date()
                try await updateUseCase?.execute(document: document)
                if selectedDocumentID == id {
                    selectedDocument = document
                }
            }
            self.renameTarget = nil
            renameText = ""
            await loadTree(selectFirstIfNeeded: false)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func requestDeleteFolder(_ node: DocumentationNode) {
        deleteTarget = .folder(id: node.id, title: displayTitle(for: node), documentIDs: documentIDs(in: node))
    }

    func requestDeleteDocument(_ node: DocumentationNode) {
        guard let documentID = node.documentId else { return }
        deleteTarget = .document(id: documentID, title: node.title)
    }

    func confirmDelete() async {
        guard let deleteTarget else { return }
        do {
            switch deleteTarget {
            case .folder(let id, _, let documentIDs):
                for documentID in documentIDs {
                    try await deleteUseCase?.execute(id: documentID)
                }
                deletedFolderIDs.insert(id)
                if let selectedDocumentID, documentIDs.contains(selectedDocumentID) {
                    self.selectedDocumentID = nil
                    selectedDocument = nil
                }
            case .document(let id, _):
                try await deleteUseCase?.execute(id: id)
                if selectedDocumentID == id {
                    selectedDocumentID = nil
                    selectedDocument = nil
                }
            }
            self.deleteTarget = nil
            await loadTree(selectFirstIfNeeded: selectedDocumentID == nil)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func duplicateDocument(_ node: DocumentationNode) async {
        guard let documentID = node.documentId, let source = documentsByID[documentID] else { return }
        let now = Date()
        var attributes = source.attributes
        attributes.createdAt = now
        attributes.lastUpdated = now
        let duplicate = DocumentationDocument(
            id: "doc-\(UUID().uuidString.lowercased())",
            title: "Copy of \(source.title)",
            summary: source.summary,
            content: source.content,
            attributes: attributes
        )

        do {
            try await createUseCase?.execute(document: duplicate)
            await handleEditorSave(documentID: duplicate.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func loadTree(selectFirstIfNeeded: Bool) async {
        isLoadingTree = true
        errorMessage = nil
        defer { isLoadingTree = false }

        do {
            let documents = try await getDocumentsUseCase?.execute() ?? []
            documentsByID = Dictionary(uniqueKeysWithValues: documents.map { ($0.id, $0) })
            let loadedTree = try await getTreeUseCase?.execute() ?? []
            tree = applySessionFolderState(to: loadedTree)
            if selectFirstIfNeeded, selectedDocumentID == nil {
                selectedDocument = nil
            } else if let selectedDocumentID {
                await selectDocument(id: selectedDocumentID)
            }
        } catch {
            tree = []
            errorMessage = error.localizedDescription
        }
    }

    private func firstDocumentID(in nodes: [DocumentationNode]) -> String? {
        for node in nodes {
            if node.type == .document, let documentId = node.documentId {
                return documentId
            }
            if let nestedID = firstDocumentID(in: node.children) {
                return nestedID
            }
        }
        return nil
    }

    private func filterTree(_ nodes: [DocumentationNode]) -> [DocumentationNode] {
        nodes.compactMap { node in
            if deletedFolderIDs.contains(node.id) { return nil }

            switch node.type {
            case .document:
                guard let documentID = node.documentId, let document = documentsByID[documentID] else { return nil }
                return filters.matches(document) ? node : nil
            case .folder:
                let children = filterTree(node.children)
                if filters.isActive, children.isEmpty { return nil }
                return DocumentationNode(
                    id: node.id,
                    title: displayTitle(for: node),
                    type: node.type,
                    children: children,
                    documentId: node.documentId
                )
            }
        }
    }

    private func applySessionFolderState(to nodes: [DocumentationNode]) -> [DocumentationNode] {
        nodes.compactMap { node in
            if deletedFolderIDs.contains(node.id) { return nil }
            return DocumentationNode(
                id: node.id,
                title: displayTitle(for: node),
                type: node.type,
                children: applySessionFolderState(to: node.children),
                documentId: node.documentId
            )
        }
    }

    private func resolvedParentFolderID(explicitParentFolderID: String?) -> String? {
        if let explicitParentFolderID {
            return explicitParentFolderID
        }
        if let selectedFolderID {
            return selectedFolderID
        }
        if let selectedDocumentID {
            return parentFolderID(forDocumentID: selectedDocumentID, in: tree)
        }
        return nil
    }

    private func parentFolderID(forDocumentID documentID: String, in nodes: [DocumentationNode], parentID: String? = nil) -> String? {
        for node in nodes {
            if node.type == .document, node.documentId == documentID {
                return parentID
            }
            if let found = parentFolderID(forDocumentID: documentID, in: node.children, parentID: node.type == .folder ? node.id : parentID) {
                return found
            }
        }
        return nil
    }

    private func documentIDs(in node: DocumentationNode) -> [String] {
        if node.type == .document {
            return node.documentId.map { [$0] } ?? []
        }
        return node.children.flatMap { documentIDs(in: $0) }
    }

    private func uniqueOptions(_ keyPath: KeyPath<DocumentationDocument, String>) -> [String] {
        documentsByID.values
            .map { $0[keyPath: keyPath].trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
            .uniqued()
            .sorted { $0.localizedStandardCompare($1) == .orderedAscending }
    }
}

struct DocumentationTreeFilters: Equatable {
    var module = "All"
    var platform = "All"
    var team = "All"
    var status: DocumentationStatus?

    var isActive: Bool {
        module != "All" || platform != "All" || team != "All" || status != nil
    }

    func matches(_ document: DocumentationDocument) -> Bool {
        if module != "All", document.attributes.module != module { return false }
        if team != "All", document.attributes.team != team, document.attributes.owner != team { return false }
        if let status, document.attributes.status != status { return false }
        if platform != "All" {
            let searchable = ([document.title, document.summary, document.attributes.module, document.attributes.team] + document.attributes.tags)
                .joined(separator: " ")
                .localizedLowercase
            if !searchable.contains(platform.localizedLowercase) { return false }
        }
        return true
    }
}

enum DocumentationRenameTarget: Identifiable, Equatable {
    case folder(id: String)
    case document(id: String)

    var id: String {
        switch self {
        case .folder(let id): "folder-\(id)"
        case .document(let id): "document-\(id)"
        }
    }
}

enum DocumentationDeleteTarget: Identifiable, Equatable {
    case folder(id: String, title: String, documentIDs: [String])
    case document(id: String, title: String)

    var id: String {
        switch self {
        case .folder(let id, _, _): "folder-\(id)"
        case .document(let id, _): "document-\(id)"
        }
    }

    var title: String {
        switch self {
        case .folder(_, let title, _), .document(_, let title):
            title
        }
    }
}

private extension Sequence where Element == String {
    func uniqued() -> [String] {
        var seen: Set<String> = []
        return filter { seen.insert($0).inserted }
    }
}
