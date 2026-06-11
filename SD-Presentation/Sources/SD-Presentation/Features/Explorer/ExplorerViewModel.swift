import Foundation
import SD_Domain
import DS_Core

@MainActor
@Observable
final class ExplorerViewModel {

    var allDocuments: [KnowledgeDocument] = []
    var grouping: DocumentGrouping = .type
    var selectedDocumentID: String? = nil
    var searchText: String = ""
    var isLoading = false
    var errorMessage: String? = nil

    // In-place editing
    var isEditingInPlace = false
    private var pendingNewDocID: String? = nil

    // New document dialog
    var showNewDocDialog = false
    var newDocTitle = ""
    var newDocType: DocumentType = .process

    var selectedDocument: KnowledgeDocument? {
        allDocuments.first { $0.id == selectedDocumentID }
    }

    var groupedSections: [(key: String, documents: [KnowledgeDocument])] {
        let filtered = searchText.isEmpty
            ? allDocuments
            : allDocuments.filter { matches($0, query: searchText) }

        let groups: [String: [KnowledgeDocument]]
        switch grouping {
        case .type:
            groups = Dictionary(grouping: filtered, by: { $0.type.rawValue.capitalized })
        case .platform:
            groups = Dictionary(grouping: filtered, by: { $0.platform.rawValue.capitalized })
        case .owner:
            groups = Dictionary(grouping: filtered, by: { $0.owner.isEmpty ? "Unassigned" : $0.owner })
        case .status:
            groups = Dictionary(grouping: filtered, by: { $0.status.rawValue.capitalized })
        }
        return groups
            .sorted { $0.key < $1.key }
            .map { (key: $0.key, documents: $0.value.sorted { $0.title < $1.title }) }
    }

    func load(services: any AppServices) async throws {
        isLoading = true
        errorMessage = nil
        do {
            allDocuments = try await services.getDocumentsUseCase.execute()
            if selectedDocumentID == nil { selectedDocumentID = allDocuments.first?.id }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    // MARK: - Document Creation

    func startCreating(title: String, type: DocumentType) {
        let doc = makeNewDocument(title: title, type: type)
        allDocuments.append(doc)
        selectedDocumentID = doc.id
        pendingNewDocID = doc.id
        isEditingInPlace = true
        showNewDocDialog = false
        newDocTitle = ""
    }

    func saveEdit(content: String) {
        guard let id = selectedDocumentID,
              let index = allDocuments.firstIndex(where: { $0.id == id }) else { return }
        allDocuments[index] = allDocuments[index].withContent(content)
        pendingNewDocID = nil
        isEditingInPlace = false
    }

    func cancelEdit() {
        if let pendingID = pendingNewDocID,
           let index = allDocuments.firstIndex(where: { $0.id == pendingID }) {
            allDocuments.remove(at: index)
            selectedDocumentID = allDocuments.first?.id
        }
        pendingNewDocID = nil
        isEditingInPlace = false
    }

    // MARK: - Helpers

    private func matches(_ doc: KnowledgeDocument, query: String) -> Bool {
        let q = query.lowercased()
        return doc.title.localizedStandardContains(q)
            || doc.summary.localizedStandardContains(q)
            || doc.content.localizedStandardContains(q)
            || doc.tags.contains { $0.localizedStandardContains(q) }
            || doc.platform.rawValue.localizedStandardContains(q)
            || doc.type.rawValue.localizedStandardContains(q)
    }

    private func makeNewDocument(title: String, type: DocumentType) -> KnowledgeDocument {
        KnowledgeDocument(
            id: UUID().uuidString,
            title: title,
            type: type,
            area: "",
            platform: .crossPlatform,
            status: .draft,
            confidence: .high,
            owner: "",
            mainContact: "",
            branches: [],
            relatedTools: [],
            relatedRepositories: [],
            summary: "",
            content: "",
            tags: [],
            lastValidated: Date(),
            nextReview: Calendar.current.date(byAdding: .month, value: 1, to: Date()) ?? Date(),
            lastUpdated: Date(),
            reviewFrequency: .monthly,
            aiReviewPriority: .medium,
            relatedTicketIDs: [],
            relatedCommitIDs: [],
            relatedReleaseIDs: [],
            relatedWorkflowIDs: [],
            aiUpdateSignals: [],
            openAIQuestions: [],
            suggestedActions: [],
            mermaidDiagram: nil
        )
    }
}

// MARK: - KnowledgeDocument copy-with-content

private extension KnowledgeDocument {
    func withContent(_ newContent: String) -> KnowledgeDocument {
        KnowledgeDocument(
            id: id, title: title, type: type, area: area, platform: platform,
            status: status == .draft ? .active : status,
            confidence: confidence, owner: owner, mainContact: mainContact,
            branches: branches, relatedTools: relatedTools,
            relatedRepositories: relatedRepositories, summary: summary,
            content: newContent, tags: tags,
            lastValidated: lastValidated, nextReview: nextReview,
            lastUpdated: Date(), reviewFrequency: reviewFrequency,
            aiReviewPriority: aiReviewPriority,
            relatedTicketIDs: relatedTicketIDs, relatedCommitIDs: relatedCommitIDs,
            relatedReleaseIDs: relatedReleaseIDs, relatedWorkflowIDs: relatedWorkflowIDs,
            aiUpdateSignals: aiUpdateSignals, openAIQuestions: openAIQuestions,
            suggestedActions: suggestedActions, mermaidDiagram: mermaidDiagram
        )
    }
}
