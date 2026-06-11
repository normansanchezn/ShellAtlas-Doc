import Testing
import Foundation
@testable import ShellDoc

@Suite("EvaluateDocumentHealthUseCase")
struct EvaluateDocumentHealthUseCaseTests {

    private let useCase = EvaluateDocumentHealthUseCase(
        ticketRepository: InMemoryTicketRepository(),
        signalRepository: InMemorySignalRepository(),
        releaseRepository: InMemoryReleaseRepository()
    )

    @Test("EoSB1 document is flagged — score below 80")
    func eosb1HealthScoreBelow80() async throws {
        let doc = outdatedEoSB1Document
        let result = try await useCase.execute(document: doc)
        #expect(result.healthScore < 80)
    }

    @Test("EoSB1 flags as reviewRequired or worse when past review date")
    func eosb1RecommendationIsNotUpToDate() async throws {
        let doc = outdatedEoSB1Document
        let result = try await useCase.execute(document: doc)
        #expect(result.recommendation != .upToDate)
    }

    @Test("EoSB1 health result has reasons")
    func eosb1HasReasons() async throws {
        let result = try await useCase.execute(document: outdatedEoSB1Document)
        #expect(!result.reasons.isEmpty)
    }

    @Test("EoSB1 health result has suggested actions")
    func eosb1HasSuggestedActions() async throws {
        let result = try await useCase.execute(document: outdatedEoSB1Document)
        #expect(!result.suggestedActions.isEmpty)
    }

    @Test("Recent valid document scores 80+")
    func recentDocumentScoresHigh() async throws {
        let doc = recentValidDocument
        let result = try await useCase.execute(document: doc)
        #expect(result.healthScore >= 80)
    }

    @Test("Document with updateconfig.py signal commit loses health points")
    func documentWithSignalCommitLosesPoints() async throws {
        let doc = outdatedEoSB1Document
        let result = try await useCase.execute(document: doc)
        let hasSignalReason = result.reasons.contains { $0.localizedStandardContains("commit") || $0.localizedStandardContains("signal") }
        #expect(hasSignalReason)
    }

    // MARK: - Test Data

    private var outdatedEoSB1Document: KnowledgeDocument {
        KnowledgeDocument(
            id: "doc-001",
            title: "EoSB1 Process for America's App - Android",
            type: .process,
            area: "Shell App",
            platform: .android,
            status: .active,
            confidence: .high,
            owner: "Android Team",
            mainContact: "Norman Sanchez",
            branches: ["develop", "extra/pilot-8.99.0"],
            relatedTools: ["GitHub Actions"],
            relatedRepositories: ["shell-android-app"],
            summary: "EoSB1 process.",
            content: "Steps involving updateconfig.py and GitHub Actions.",
            tags: ["eosb", "release"],
            lastValidated: Date(timeIntervalSinceNow: -150 * 24 * 3600),
            nextReview: Date(timeIntervalSinceNow: -50 * 24 * 3600),
            lastUpdated: Date(timeIntervalSinceNow: -150 * 24 * 3600),
            reviewFrequency: .quarterly,
            aiReviewPriority: .high,
            relatedTicketIDs: ["t-001"],
            relatedCommitIDs: ["c-001"],
            relatedReleaseIDs: [],
            relatedWorkflowIDs: ["w-001"],
            aiUpdateSignals: ["Changes in updateconfig.py", "Changes in build.gradle"],
            openAIQuestions: ["Has pilot branch naming changed?"],
            suggestedActions: [],
            mermaidDiagram: nil
        )
    }

    private var recentValidDocument: KnowledgeDocument {
        KnowledgeDocument(
            id: "doc-recent",
            title: "Recently Validated Document",
            type: .guide,
            area: "Test",
            platform: .crossPlatform,
            status: .active,
            confidence: .high,
            owner: "Test Team",
            mainContact: "Test User",
            branches: ["develop"],
            relatedTools: [],
            relatedRepositories: [],
            summary: "Up to date document.",
            content: "No signals match.",
            tags: [],
            lastValidated: Date(timeIntervalSinceNow: -5 * 24 * 3600),
            nextReview: Date(timeIntervalSinceNow: 85 * 24 * 3600),
            lastUpdated: Date(timeIntervalSinceNow: -5 * 24 * 3600),
            reviewFrequency: .quarterly,
            aiReviewPriority: .low,
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

// MARK: - In-memory test repositories

struct InMemoryTicketRepository: TicketRepository {
    func getTickets() async throws -> [KnowledgeTicket] {
        [
            KnowledgeTicket(
                id: "t-001",
                title: "Update EoSB1 for new versionCode strategy",
                type: .task,
                status: .closed,
                closedDate: Date(timeIntervalSinceNow: -80 * 24 * 3600),
                description: "updateconfig.py changes",
                platform: .android,
                relatedDocumentIDs: ["doc-001"]
            )
        ]
    }
}

struct InMemorySignalRepository: RepositorySignalRepository {
    func getCommits() async throws -> [RepositoryCommit] {
        [
            RepositoryCommit(
                id: "c-001",
                sha: "abc123def456",
                message: "chore: update updateconfig.py",
                author: "Carlos Mendez",
                date: Date(timeIntervalSinceNow: -100 * 24 * 3600),
                changedFiles: ["updateconfig.py"],
                repository: "shell-android-app",
                branch: "develop",
                relatedDocumentIDs: ["doc-001"]
            )
        ]
    }

    func getWorkflowChanges() async throws -> [WorkflowChange] {
        [
            WorkflowChange(
                id: "w-001",
                workflowName: "eosb1-build.yml",
                changedAt: Date(timeIntervalSinceNow: -90 * 24 * 3600),
                description: "Updated EoSB1 workflow",
                changedFiles: [".github/workflows/eosb1-build.yml"],
                relatedDocumentIDs: ["doc-001"]
            )
        ]
    }
}

struct InMemoryReleaseRepository: ReleaseRepository {
    func getReleases() async throws -> [ReleaseNote] { [] }
}
