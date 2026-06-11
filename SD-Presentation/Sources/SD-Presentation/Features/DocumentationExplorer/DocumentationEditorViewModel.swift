import Foundation
import Observation
import SD_Domain

@Observable
@MainActor
final class DocumentationEditorViewModel {
    let mode: DocumentationEditorMode

    var title: String = ""
    var summary: String = ""
    var content: String = ""
    var owner: String = ""
    var module: String = ""
    var team: String = ""
    var status: DocumentationStatus = .draft
    var tagsText: String = ""
    var saveState: DocumentSaveState = .idle
    var isSaving = false
    var isLoadingDocument = false
    var isSavingDraft = false
    var isLoadingVersions = false
    var errorMessage: String?
    var draftMessage: String?
    var didLoad = false
    var versions: [DocumentationVersion] = []

    var canSave: Bool {
        !trimmed(title).isEmpty
            && !trimmed(content).isEmpty
            && !trimmed(owner).isEmpty
            && !trimmed(module).isEmpty
            && !trimmed(team).isEmpty
            && hasUnsavedChanges
            && !isSaving
            && !isLoadingDocument
    }

    var hasUnsavedChanges: Bool {
        currentSnapshot != originalSnapshot
    }

    var effectiveSaveState: DocumentSaveState {
        if isSaving { return .saving }
        if errorMessage != nil, saveState == .failed { return .failed }
        if hasUnsavedChanges { return .dirty }
        return saveState
    }

    var primarySaveButtonTitle: String {
        switch effectiveSaveState {
        case .saving:
            "Saving..."
        case .saved:
            "Saved"
        case .failed:
            "Retry Save"
        case .idle, .dirty:
            switch mode {
            case .create: "Create Document"
            case .edit: "Save Changes"
            }
        }
    }

    var validationMessages: [String] {
        var messages: [String] = []
        if trimmed(title).isEmpty { messages.append("Title is required.") }
        if trimmed(content).isEmpty { messages.append("Content is required.") }
        if trimmed(owner).isEmpty { messages.append("Owner is required.") }
        if trimmed(module).isEmpty { messages.append("Module is required.") }
        if trimmed(team).isEmpty { messages.append("Team is required.") }
        return messages
    }

    var tags: [String] {
        parsedTags()
    }

    private var originalDocument: DocumentationDocument?
    private var originalSnapshot = EditorSnapshot.empty
    private var getDocumentUseCase: (any GetDocumentationDocumentDetailUseCaseProtocol)?
    private var createUseCase: (any CreateDocumentUseCaseProtocol)?
    private var updateUseCase: (any UpdateDocumentUseCaseProtocol)?
    private var saveDraftUseCase: (any SaveDocumentationDraftUseCaseProtocol)?
    private var getVersionsUseCase: (any GetDocumentationVersionsUseCaseProtocol)?
    private var restoreVersionUseCase: (any RestoreDocumentationVersionUseCaseProtocol)?

    init(mode: DocumentationEditorMode) {
        self.mode = mode
    }

    func configure(services: any AppServices) {
        getDocumentUseCase = services.getDocumentationDocumentDetailUseCase
        createUseCase = services.createDocumentUseCase
        updateUseCase = services.updateDocumentUseCase
        saveDraftUseCase = services.saveDocumentationDraftUseCase
        getVersionsUseCase = services.getDocumentationVersionsUseCase
        restoreVersionUseCase = services.restoreDocumentationVersionUseCase
    }

    func onAppear() async {
        guard !didLoad else { return }
        didLoad = true
        await loadDocumentIfNeeded()
        await loadVersions()
    }

    func loadDocumentIfNeeded() async {
        guard case .edit(let documentId) = mode else {
            originalSnapshot = currentSnapshot
            saveState = .idle
            return
        }

        isLoadingDocument = true
        defer { isLoadingDocument = false }
        do {
            let document = try await getDocumentUseCase?.execute(id: documentId)
            guard let document else { return }
            originalDocument = document
            apply(document)
            originalSnapshot = currentSnapshot
            saveState = .idle
        } catch {
            errorMessage = error.localizedDescription
            saveState = .failed
        }
    }

    func save() async -> String? {
        guard canSave else { return nil }
        isSaving = true
        saveState = .saving
        errorMessage = nil
        defer { isSaving = false }

        do {
            let document = makeDocumentForSave()
            switch mode {
            case .create:
                try await createUseCase?.execute(document: document)
            case .edit:
                try await updateUseCase?.execute(document: document)
            }
            originalDocument = document
            originalSnapshot = currentSnapshot
            saveState = .saved
            await loadVersions()
            return document.id
        } catch {
            errorMessage = error.localizedDescription
            saveState = .failed
            return nil
        }
    }

    func validate() -> Bool {
        canSave
    }

    func autosaveDraftIfPossible() async {
        guard case .edit(let documentId) = mode else { return }
        guard hasUnsavedChanges, !trimmed(content).isEmpty, !isSavingDraft else { return }
        isSavingDraft = true
        draftMessage = "Saving draft..."
        defer { isSavingDraft = false }

        do {
            _ = try await saveDraftUseCase?.execute(documentId: documentId, rawMarkdown: content)
            draftMessage = "Draft saved"
        } catch {
            draftMessage = "Draft save failed"
        }
    }

    func loadVersions() async {
        guard case .edit(let documentId) = mode else { return }
        isLoadingVersions = true
        defer { isLoadingVersions = false }
        do {
            versions = try await getVersionsUseCase?.execute(documentId: documentId) ?? []
        } catch {
            versions = []
        }
    }

    func restoreVersion(_ version: DocumentationVersion) async {
        guard case .edit(let documentId) = mode else { return }
        isLoadingDocument = true
        defer { isLoadingDocument = false }
        do {
            let document = try await restoreVersionUseCase?.execute(documentId: documentId, versionId: version.id)
            guard let document else { return }
            apply(document)
            originalDocument = document
            originalSnapshot = currentSnapshot
            saveState = .saved
            await loadVersions()
        } catch {
            errorMessage = error.localizedDescription
            saveState = .failed
        }
    }

    private func makeDocumentForSave() -> DocumentationDocument {
        let now = Date()
        let tags = parsedTags()

        let id: String
        let createdAt: Date
        switch mode {
        case .create:
            id = "doc-\(UUID().uuidString.lowercased())"
            createdAt = now
        case .edit:
            id = originalDocument?.id ?? "doc-\(UUID().uuidString.lowercased())"
            createdAt = originalDocument?.attributes.createdAt ?? now
        }

        return DocumentationDocument(
            id: id,
            title: trimmed(title),
            summary: trimmed(summary),
            content: trimmed(content),
            attributes: DocumentationAttributes(
                owner: trimmed(owner),
                module: trimmed(module),
                team: trimmed(team),
                status: status,
                tags: tags,
                lastUpdated: now,
                createdAt: createdAt,
                parentFolderId: parentFolderIdForSave
            )
        )
    }

    private var parentFolderIdForSave: String? {
        switch mode {
        case .create(let parentFolderId):
            parentFolderId
        case .edit:
            originalDocument?.attributes.parentFolderId
        }
    }

    private func apply(_ document: DocumentationDocument) {
        title = document.title
        summary = document.summary
        content = document.content
        owner = document.attributes.owner
        module = document.attributes.module
        team = document.attributes.team
        status = document.attributes.status
        tagsText = document.attributes.tags.joined(separator: ", ")
    }

    private func trimmed(_ value: String) -> String {
        value.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func parsedTags() -> [String] {
        tagsText
            .split(separator: ",")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }

    private var currentSnapshot: EditorSnapshot {
        EditorSnapshot(
            title: trimmed(title),
            summary: trimmed(summary),
            content: trimmed(content),
            owner: trimmed(owner),
            module: trimmed(module),
            team: trimmed(team),
            status: status,
            tags: parsedTags()
        )
    }
}

enum DocumentSaveState: Equatable {
    case idle
    case dirty
    case saving
    case saved
    case failed

    var displayName: String {
        switch self {
        case .idle: "No changes"
        case .dirty: "Unsaved changes"
        case .saving: "Saving..."
        case .saved: "Saved"
        case .failed: "Failed to save"
        }
    }
}

private struct EditorSnapshot: Equatable {
    let title: String
    let summary: String
    let content: String
    let owner: String
    let module: String
    let team: String
    let status: DocumentationStatus
    let tags: [String]

    static let empty = EditorSnapshot(
        title: "",
        summary: "",
        content: "",
        owner: "",
        module: "",
        team: "",
        status: .draft,
        tags: []
    )
}
