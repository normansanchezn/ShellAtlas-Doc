import Foundation
import Testing
@testable import SD_Domain

@Test func searchDocumentsByTitleUseCaseTrimsBlankQueries() async throws {
    let repository = StubDocumentationRepository()
    let useCase = SearchDocumentsByTitleUseCase(repository: repository)

    let results = try await useCase.execute(query: "   ")

    #expect(results.isEmpty)
}

@Test func getDocumentationTreeUseCaseReturnsRepositoryTree() async throws {
    let node = DocumentationNode(id: "root", title: "Root", type: .folder)
    let repository = StubDocumentationTreeRepository(tree: [node])
    let useCase = GetDocumentationTreeUseCase(repository: repository)

    let tree = try await useCase.execute()

    #expect(tree == [node])
}

@Test func documentationReviewPolicyMarksDocumentsOlderThanOneYear() {
    let calendar = Calendar(identifier: .gregorian)
    let now = calendar.date(from: DateComponents(year: 2026, month: 6, day: 5))!
    let lastUpdated = calendar.date(from: DateComponents(year: 2025, month: 5, day: 1))!
    let document = makeDocument(status: .published, lastUpdated: lastUpdated)

    #expect(DocumentationReviewPolicy.updatesPending(document, now: now))
    #expect(DocumentationReviewPolicy.effectiveStatus(for: document, now: now) == .updatesPending)
}

@Test func documentationReviewPolicyHonorsExplicitUpdatesPendingStatus() {
    let document = makeDocument(status: .updatesPending, lastUpdated: Date())

    #expect(DocumentationReviewPolicy.updatesPending(document))
    #expect(DocumentationReviewPolicy.updateReason(for: document) == "Marked for update")
}

@Test func documentationAssistantPromptBuilderCreatesGroundedPrompt() {
    let builder = DefaultDocumentationAssistantPromptBuilder()
    let snippet = DocumentationSnippet(
        id: "snippet-1",
        documentId: "doc-1",
        documentTitle: "End of Sprint Build",
        text: "Attach build notes to the QA handoff.",
        score: 45,
        sectionTitle: "Checklist"
    )

    let prompt = builder.buildPrompt(question: "What goes in the handoff?", snippets: [snippet])

    #expect(prompt.contains("You are an intelligent documentation assistant"))
    #expect(prompt.contains("Prefer a useful grounded synthesis over a terse refusal."))
    #expect(prompt.contains("Do not invent owners, tickets, commits"))
    #expect(prompt.contains("Source: End of Sprint Build"))
    #expect(prompt.contains("Document ID: doc-1"))
    #expect(prompt.contains("User Question:\nWhat goes in the handoff?"))
}

@Test func documentationAssistantPromptBuilderUsesSpanishAndInfersProcessDiagram() {
    let builder = DefaultDocumentationAssistantPromptBuilder()
    let snippet = DocumentationSnippet(
        id: "snippet-1",
        documentId: "doc-1",
        documentTitle: "End of Sprint Build",
        text: """
        ## Checklist
        - Confirm release branch is green.
        - Attach build notes to the QA handoff.
        """,
        score: 45,
        sectionTitle: "Checklist"
    )

    let prompt = builder.buildPrompt(question: "Que es un End of Sprint Build?", snippets: [snippet])

    #expect(prompt.contains("Reply in Spanish"))
    #expect(prompt.contains("Include a valid Mermaid diagram automatically"))
    #expect(prompt.contains("EoSB / EoSB1"))
    #expect(prompt.contains("Prefer a useful grounded synthesis"))
}

@Test func documentationAssistantIntentClassifierRecognizesAcronymAndReleaseQuestions() {
    let classifier = DocumentationAssistantIntentClassifier()

    #expect(classifier.classify(question: "Que significa EoSB?", snippets: []) == .acronymQuestion)
    #expect(classifier.classify(question: "How does the build go to QA?", snippets: []) == .releaseOrBuildQuestion)
}

@Test func documentationAssistantPromptBuilderHonorsExplicitLanguageRequest() {
    let builder = DefaultDocumentationAssistantPromptBuilder()
    let snippet = DocumentationSnippet(
        id: "snippet-1",
        documentId: "doc-1",
        documentTitle: "Rewards Flow",
        text: "Rewards are redeemed from the app.",
        score: 20
    )

    let prompt = builder.buildPrompt(question: "Answer in Spanish: what is the rewards flow?", snippets: [snippet])

    #expect(prompt.contains("Reply in Spanish because the user explicitly requested it."))
}

@Test func askDocumentationAssistantUseCaseReturnsFallbackWhenRetrievalIsEmpty() async throws {
    let retrieval = StubDocumentationRetrievalRepository(snippets: [])
    let useCase = AskDocumentationAssistantUseCase(
        retrievalUseCase: RetrieveDocumentationSnippetsUseCase(repository: retrieval),
        promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
        localLLMClient: StubLocalLLMClient(answer: "Should not be used")
    )

    let answer = try await useCase.execute(question: DocumentationQuestion(text: "Unknown process"))

    #expect(answer.confidence == .notEnoughInformation)
    #expect(answer.sources.isEmpty)
    #expect(answer.text == DocumentationAssistantDefaults.notEnoughInformationMessage)
}

@Test func askDocumentationAssistantUseCaseLocalizesFallbackWhenQuestionIsSpanish() async throws {
    let retrieval = StubDocumentationRetrievalRepository(snippets: [])
    let useCase = AskDocumentationAssistantUseCase(
        retrievalUseCase: RetrieveDocumentationSnippetsUseCase(repository: retrieval),
        promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
        localLLMClient: StubLocalLLMClient(answer: "Should not be used")
    )

    let answer = try await useCase.execute(question: DocumentationQuestion(text: "Que proceso existe para esto?"))

    #expect(answer.confidence == .notEnoughInformation)
    #expect(answer.text == "La documentación no contiene suficiente información para responder esa pregunta.")
}

@Test func askDocumentationAssistantUseCaseReturnsAnswerWithSources() async throws {
    let snippet = DocumentationSnippet(
        id: "snippet-1",
        documentId: "doc-1",
        documentTitle: "QA Validation",
        text: "QA validates release candidates against sprint scope.",
        score: 25
    )
    let retrieval = StubDocumentationRetrievalRepository(snippets: [snippet])
    let useCase = AskDocumentationAssistantUseCase(
        retrievalUseCase: RetrieveDocumentationSnippetsUseCase(repository: retrieval),
        promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
        localLLMClient: StubLocalLLMClient(answer: "QA validates release candidates against sprint scope.")
    )

    let answer = try await useCase.execute(question: DocumentationQuestion(text: "What does QA validate?"))

    #expect(answer.text == "QA validates release candidates against sprint scope.")
    #expect(answer.sources == [
        DocumentationSource(id: "snippet-1", documentId: "doc-1", title: "QA Validation")
    ])
    #expect(answer.confidence == .medium)
}

@Test func checkDocumentationAssistantStatusUseCaseReturnsRepositoryStatus() async {
    let expected = DocumentationAssistantStatus(
        isOllamaRunning: true,
        isModelInstalled: true,
        modelName: "qwen2.5-coder:7b",
        message: "Ready"
    )
    let useCase = CheckDocumentationAssistantStatusUseCase(
        repository: StubLocalLLMStatusRepository(status: expected)
    )

    let status = await useCase.execute()

    #expect(status == expected)
    #expect(status.isReady)
}

private struct StubDocumentationTreeRepository: DocumentationTreeRepository {
    let tree: [DocumentationNode]

    func getDocumentationTree() async throws -> [DocumentationNode] {
        tree
    }
}

private struct StubDocumentationRepository: DocumentationRepository {
    func getDocuments() async throws -> [DocumentationDocument] {
        []
    }

    func getDocument(id: String) async throws -> DocumentationDocument {
        throw DomainError.documentNotFound(id)
    }

    func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument] {
        [
            DocumentationDocument(
                id: "match",
                title: query,
                summary: "",
                content: query,
                attributes: DocumentationAttributes(
                    owner: "Owner",
                    module: "Module",
                    team: "Team",
                    status: .draft,
                    tags: [],
                    lastUpdated: Date(),
                    createdAt: Date()
                )
            )
        ]
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

private struct StubDocumentationRetrievalRepository: DocumentationRetrievalRepository {
    let snippets: [DocumentationSnippet]

    func retrieveRelevantSnippets(for question: DocumentationQuestion, limit: Int) async throws -> [DocumentationSnippet] {
        Array(snippets.prefix(limit))
    }
}

private struct StubLocalLLMClient: LocalLLMClient {
    let answer: String

    func generateAnswer(prompt: String) async throws -> String {
        answer
    }
}

private struct StubLocalLLMStatusRepository: LocalLLMStatusRepository {
    let status: DocumentationAssistantStatus

    func checkStatus() async -> DocumentationAssistantStatus {
        status
    }
}

private func makeDocument(status: DocumentationStatus, lastUpdated: Date) -> DocumentationDocument {
    DocumentationDocument(
        id: "doc",
        title: "Document",
        summary: "",
        content: "# Document",
        attributes: DocumentationAttributes(
            owner: "Owner",
            module: "Module",
            team: "Team",
            status: status,
            tags: [],
            lastUpdated: lastUpdated,
            createdAt: lastUpdated
        )
    )
}
