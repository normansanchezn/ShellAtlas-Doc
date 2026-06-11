import Foundation
import Testing
import SD_Domain
@testable import SD_Data

@Test func mockDocumentationTreeContainsShellAppDocumentation() async throws {
    let repository = MockDocumentationTreeRepository(store: testDocumentationStore())

    let tree = try await repository.getDocumentationTree()

    let shellAppNode = tree.first { $0.title == "Shell App Documentation" }
    #expect(shellAppNode?.children.contains { $0.title == "Android" } == true)
}

@Test func mockDocumentationDocumentsIncludeExplicitAndStaleUpdatesPendingDocuments() async throws {
    let repository = MockDocumentationRepository(store: testDocumentationStore())

    let documents = try await repository.getDocuments()
    let pendingTitles = documents
        .filter { DocumentationReviewPolicy.updatesPending($0) }
        .map(\.title)

    #expect(pendingTitles.contains("In-Progress Offers"))
    #expect(pendingTitles.contains("Fuel Rewards Error States"))
    #expect(pendingTitles.contains("iOS Payment Sheet"))
    #expect(pendingTitles.contains("Station Locator Cache Policy"))
}

@Test func mockDocumentationRepositorySearchesByTitleOnly() async throws {
    let repository = MockDocumentationRepository(store: testDocumentationStore())

    let titleResults = try await repository.searchDocumentsByTitle("Rewards")
    let contentOnlyResults = try await repository.searchDocumentsByTitle("transient failures")

    #expect(titleResults.contains { $0.title == "Rewards Flow" })
    #expect(contentOnlyResults.isEmpty)
}

@Test func mockDocumentationRepositoryCreatesAndUpdatesDocument() async throws {
    let repository = MockDocumentationRepository(store: testDocumentationStore())
    let id = "test-\(UUID().uuidString)"
    let now = Date()
    let document = DocumentationDocument(
        id: id,
        title: "Temporary Test Doc",
        summary: "Created by repository test.",
        content: "# Temporary Test Doc",
        attributes: DocumentationAttributes(
            owner: "Test Owner",
            module: "Tests",
            team: "Product Engineering",
            status: .draft,
            tags: ["test"],
            lastUpdated: now,
            createdAt: now
        )
    )

    try await repository.createDocument(document)

    var updated = try await repository.getDocument(id: id)
    updated.title = "Updated Temporary Test Doc"
    updated.attributes.status = .published
    try await repository.updateDocument(updated)

    let result = try await repository.getDocument(id: id)
    #expect(result.title == "Updated Temporary Test Doc")
    #expect(result.attributes.status == .published)
}

@Test func localDocumentationRetrievalScoresAndRanksTitleMatchesFirst() async throws {
    let repository = LocalDocumentationRetrievalRepository(
        documentationRepository: StubDocumentationRepository(documents: [
            makeAssistantDocument(
                id: "content",
                title: "Generic Release Notes",
                summary: "Mentions sprint builds.",
                content: "End of sprint build notes are archived here.",
                tags: ["release"]
            ),
            makeAssistantDocument(
                id: "title",
                title: "End of Sprint Build",
                summary: "Checklist for validation builds.",
                content: "Attach build notes to the QA handoff.",
                tags: ["android", "qa"]
            )
        ])
    )

    let snippets = try await repository.retrieveRelevantSnippets(
        for: DocumentationQuestion(text: "End of Sprint Build"),
        limit: 5
    )

    #expect(snippets.first?.documentId == "title")
    #expect((snippets.first?.score ?? 0) > (snippets.last?.score ?? 0))
}

@Test func localDocumentationRetrievalReturnsEmptyForUnsupportedQuestion() async throws {
    let repository = LocalDocumentationRetrievalRepository(
        documentationRepository: StubDocumentationRepository(documents: [
            makeAssistantDocument(
                id: "doc",
                title: "Receipts",
                summary: "Receipt presentation rules.",
                content: "Receipts summarize completed fuel transactions.",
                tags: ["receipts"]
            )
        ])
    )

    let snippets = try await repository.retrieveRelevantSnippets(
        for: DocumentationQuestion(text: "How are deployment approvals handled?"),
        limit: 5
    )

    #expect(snippets.isEmpty)
}

@Test func localDocumentationRetrievalUsesTagsModuleOwnerAndTeam() async throws {
    let repository = LocalDocumentationRetrievalRepository(
        documentationRepository: StubDocumentationRepository(documents: [
            makeAssistantDocument(
                id: "loyalty",
                title: "Rewards Flow",
                summary: "Enrollment and redemption behavior.",
                content: "Eligible rewards appear before informational tiles.",
                owner: "Maya Chen",
                module: "Loyalty",
                team: "Android Shell App",
                tags: ["android", "loyalty", "rewards"]
            )
        ])
    )

    let snippets = try await repository.retrieveRelevantSnippets(
        for: DocumentationQuestion(text: "loyalty android"),
        limit: 3
    )

    #expect(snippets.count == 1)
    #expect(snippets[0].documentId == "loyalty")
    #expect(snippets[0].text.contains("Module: Loyalty"))
    #expect(snippets[0].text.contains("Team: Android Shell App"))
}

@Test func localDocumentationRetrievalExpandsAssistantAcronyms() async throws {
    let repository = LocalDocumentationRetrievalRepository(
        documentationRepository: StubDocumentationRepository(documents: [
            makeAssistantDocument(
                id: "eosb",
                title: "End of Sprint Build",
                summary: "Checklist for Android validation builds.",
                content: "Release branch, QA handoff, and install instructions.",
                module: "Release Process",
                team: "Mobile Release",
                tags: ["android", "release", "qa"]
            )
        ])
    )

    let snippets = try await repository.retrieveRelevantSnippets(
        for: DocumentationQuestion(text: "Que es EoSB1?"),
        limit: 3
    )

    #expect(snippets.count == 2)
    #expect(snippets[0].documentId == "internal-acronyms-glossary")
    #expect(snippets[0].text.contains("End of Sprint Build 1"))
    #expect(snippets[1].documentId == "eosb")
}

@Test func localDocumentationRetrievalCanReturnGlossaryOnlyForAcronymQuestions() async throws {
    let repository = LocalDocumentationRetrievalRepository(
        documentationRepository: StubDocumentationRepository(documents: [])
    )

    let snippets = try await repository.retrieveRelevantSnippets(
        for: DocumentationQuestion(text: "Que significa PI?"),
        limit: 3
    )

    #expect(snippets.count == 1)
    #expect(snippets[0].documentTitle == "Internal Acronyms Glossary")
    #expect(snippets[0].text.contains("Program Increment"))
}

@Test func unavailableLocalLLMClientMapsToLocalConnectionError() async throws {
    let client = UnavailableLocalLLMClient()

    await #expect(throws: OllamaLocalLLMError.connectionFailed) {
        _ = try await client.generateAnswer(prompt: "Prompt")
    }
}

private func testDocumentationStore() -> DocumentationMockStore {
    DocumentationMockStore(documents: DocumentationMockData.documents, persistsChanges: false)
}

private struct StubDocumentationRepository: DocumentationRepository {
    let documents: [DocumentationDocument]

    func getDocuments() async throws -> [DocumentationDocument] {
        documents
    }

    func getDocument(id: String) async throws -> DocumentationDocument {
        guard let document = documents.first(where: { $0.id == id }) else {
            throw DomainError.documentNotFound(id)
        }
        return document
    }

    func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument] {
        documents.filter { $0.title.localizedStandardContains(query) }
    }

    func createDocument(_ document: DocumentationDocument) async throws {}

    func updateDocument(_ document: DocumentationDocument) async throws {}

    func deleteDocument(id: String) async throws {}

    func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        DocumentationDraftReceipt(documentId: documentId, draftSaved: true, contentHash: "stub", updatedAt: Date())
    }

    func getVersions(documentId: String) async throws -> [DocumentationVersion] {
        []
    }

    func restoreVersion(documentId: String, versionId: String) async throws -> DocumentationDocument {
        throw DomainError.documentNotFound(documentId)
    }
}

private func makeAssistantDocument(
    id: String,
    title: String,
    summary: String,
    content: String,
    owner: String = "Owner",
    module: String = "Module",
    team: String = "Team",
    tags: [String]
) -> DocumentationDocument {
    DocumentationDocument(
        id: id,
        title: title,
        summary: summary,
        content: content,
        attributes: DocumentationAttributes(
            owner: owner,
            module: module,
            team: team,
            status: .published,
            tags: tags,
            lastUpdated: Date(),
            createdAt: Date()
        )
    )
}
