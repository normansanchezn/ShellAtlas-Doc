import Testing
@testable import ShellDoc

@Suite("SearchKnowledgeUseCase")
struct SearchKnowledgeUseCaseTests {

    private let useCase = SearchKnowledgeUseCase(repository: InMemoryDocumentRepository())

    @Test("Search 'release build' finds EoSB1 document")
    func searchReleaseBuildFindsEoSB1() async throws {
        let results = try await useCase.execute(query: "release build")
        #expect(results.contains { $0.id == "test-eosb1" })
    }

    @Test("Search 'localization' finds Lokalise document")
    func searchLocalizationFindsLokalise() async throws {
        let results = try await useCase.execute(query: "localization")
        #expect(results.contains { $0.id == "test-lokalise" })
    }

    @Test("Search 'pilot branch' finds EoSB1 document")
    func searchPilotBranchFindsEoSB1() async throws {
        let results = try await useCase.execute(query: "pilot branch")
        #expect(results.contains { $0.id == "test-eosb1" })
    }

    @Test("Empty query returns empty results")
    func emptyQueryReturnsEmpty() async throws {
        let results = try await useCase.execute(query: "")
        #expect(results.isEmpty)
    }

    @Test("Search 'secrets' finds Azure Secrets document")
    func searchSecretsFindsAzureDocument() async throws {
        let results = try await useCase.execute(query: "secrets")
        #expect(results.contains { $0.id == "test-secrets" })
    }

    @Test("Search 'eosb' directly finds EoSB1 document")
    func searchEosbDirectly() async throws {
        let results = try await useCase.execute(query: "eosb")
        #expect(results.contains { $0.id == "test-eosb1" })
    }

    @Test("search() helper filters documents synchronously")
    func searchHelperFiltersDocuments() {
        let docs = InMemoryDocumentRepository.testDocuments
        let results = useCase.search(documents: docs, query: "release build")
        #expect(results.contains { $0.id == "test-eosb1" })
    }
}

// MARK: - In-Memory test doubles

struct InMemoryDocumentRepository: KnowledgeDocumentRepository {
    static let testDocuments: [KnowledgeDocument] = [
        KnowledgeDocument(
            id: "test-eosb1",
            title: "EoSB1 Process for America's App - Android",
            type: .process,
            area: "Shell App",
            platform: .android,
            status: .active,
            confidence: .high,
            owner: "Android Team",
            mainContact: "Norman Sanchez",
            branches: ["develop", "extra/pilot-8.99.0", "madf/pilot"],
            relatedTools: ["GitHub", "GitHub Actions", "Lokalise"],
            relatedRepositories: ["shell-android-app"],
            summary: "End-of-Sprint Build process. Covers build generation, QA handoff, pilot branch strategy.",
            content: "Steps: updateconfig.py, GitHub Actions build, QA handoff, pilot branch creation.",
            tags: ["android", "release", "eosb", "qa", "build", "eosb1"],
            lastValidated: Date(timeIntervalSinceNow: -180 * 24 * 3600),
            nextReview: Date(timeIntervalSinceNow: -30 * 24 * 3600),
            lastUpdated: Date(timeIntervalSinceNow: -180 * 24 * 3600),
            reviewFrequency: .quarterly,
            aiReviewPriority: .high,
            relatedTicketIDs: ["t-001"],
            relatedCommitIDs: ["c-001", "c-002"],
            relatedReleaseIDs: [],
            relatedWorkflowIDs: ["w-001"],
            aiUpdateSignals: ["Changes in build.gradle", "Changes in updateconfig.py"],
            openAIQuestions: ["Has the pilot branch naming changed?"],
            suggestedActions: ["Review updateconfig.py"],
            mermaidDiagram: nil
        ),
        KnowledgeDocument(
            id: "test-lokalise",
            title: "Lokalise Strings Update Process",
            type: .process,
            area: "Shell App",
            platform: .crossPlatform,
            status: .active,
            confidence: .medium,
            owner: "Mobile Platform Team",
            mainContact: "Sara Kim",
            branches: ["develop"],
            relatedTools: ["Lokalise", "GitHub Actions"],
            relatedRepositories: ["shell-android-app"],
            summary: "Process for updating and exporting localization strings via Lokalise.",
            content: "Steps: add string in Lokalise, request translation, export via GitHub Actions workflow.",
            tags: ["localization", "lokalise", "strings", "translations", "l10n", "strings.xml"],
            lastValidated: Date(timeIntervalSinceNow: -60 * 24 * 3600),
            nextReview: Date(timeIntervalSinceNow: 30 * 24 * 3600),
            lastUpdated: Date(timeIntervalSinceNow: -60 * 24 * 3600),
            reviewFrequency: .quarterly,
            aiReviewPriority: .medium,
            relatedTicketIDs: [],
            relatedCommitIDs: [],
            relatedReleaseIDs: [],
            relatedWorkflowIDs: [],
            aiUpdateSignals: ["Changes in Lokalise export workflow"],
            openAIQuestions: [],
            suggestedActions: [],
            mermaidDiagram: nil
        ),
        KnowledgeDocument(
            id: "test-secrets",
            title: "Azure Secrets Management for Mobile",
            type: .guide,
            area: "Shell App",
            platform: .crossPlatform,
            status: .active,
            confidence: .high,
            owner: "DevOps Team",
            mainContact: "Carlos Mendez",
            branches: ["develop"],
            relatedTools: ["Azure Secrets", "Keychain", "GitHub Actions"],
            relatedRepositories: ["shell-devops-config"],
            summary: "Guide for managing mobile secrets in Azure Key Vault.",
            content: "Azure secrets, Keychain, environment values configuration and rotation.",
            tags: ["azure secrets", "keychain", "secrets", "security", "credentials", "environment values"],
            lastValidated: Date(timeIntervalSinceNow: -10 * 24 * 3600),
            nextReview: Date(timeIntervalSinceNow: 170 * 24 * 3600),
            lastUpdated: Date(timeIntervalSinceNow: -10 * 24 * 3600),
            reviewFrequency: .semiannual,
            aiReviewPriority: .critical,
            relatedTicketIDs: [],
            relatedCommitIDs: [],
            relatedReleaseIDs: [],
            relatedWorkflowIDs: [],
            aiUpdateSignals: ["New Azure secrets added"],
            openAIQuestions: [],
            suggestedActions: [],
            mermaidDiagram: nil
        )
    ]

    func getDocuments() async throws -> [KnowledgeDocument] { Self.testDocuments }
    func getDocument(id: String) async throws -> KnowledgeDocument? {
        Self.testDocuments.first { $0.id == id }
    }
}
