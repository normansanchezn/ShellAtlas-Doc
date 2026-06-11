import Foundation
import DS_Core


public struct AnswerQuestionUseCase: Sendable {
    let documentRepository: any KnowledgeDocumentRepository
    let ticketRepository: any TicketRepository
    let signalRepository: any RepositorySignalRepository
    let releaseRepository: any ReleaseRepository
    let searchUseCase: SearchKnowledgeUseCase

    public init(
        documentRepository: any KnowledgeDocumentRepository,
        ticketRepository: any TicketRepository,
        signalRepository: any RepositorySignalRepository,
        releaseRepository: any ReleaseRepository,
        searchUseCase: SearchKnowledgeUseCase
    ) {
        self.documentRepository = documentRepository
        self.ticketRepository = ticketRepository
        self.signalRepository = signalRepository
        self.releaseRepository = releaseRepository
        self.searchUseCase = searchUseCase
    }

    public func execute(question: String) async throws -> AssistantAnswer {
        async let allDocs = documentRepository.getDocuments()
        async let tickets = ticketRepository.getTickets()
        async let commits = signalRepository.getCommits()
        async let workflows = signalRepository.getWorkflowChanges()
        async let releases = releaseRepository.getReleases()

        let (docs, t, c, w, r) = try await (allDocs, tickets, commits, workflows, releases)

        let relevantDocs = searchUseCase.search(documents: docs, query: question)
        let topDocs = Array(relevantDocs.prefix(3))

        let relatedSignals = buildSignals(
            forDocuments: topDocs, tickets: t, commits: c, workflows: w, releases: r
        )

        let summary = buildSummary(question: question, documents: topDocs, signals: relatedSignals)
        let issues = detectIssues(documents: topDocs)
        let actions = suggestActions(question: question, documents: topDocs)

        return AssistantAnswer(
            summary: summary,
            relevantDocuments: topDocs,
            relatedSignals: relatedSignals,
            potentialIssues: issues,
            suggestedActions: actions
        )
    }

    private func buildSummary(question: String, documents: [KnowledgeDocument], signals: [KnowledgeSignal]) -> String {
        if documents.isEmpty {
            return "I could not find documents matching your question. Try rephrasing using terms like 'EoSB1', 'Lokalise', 'authentication', 'deep link', or 'Azure secrets'."
        }

        let docTitles = documents.map { "**\($0.title)**" }.joined(separator: ", ")
        var summary = "Based on the knowledge base, I found \(documents.count) relevant document(s): \(docTitles).\n\n"

        if let primary = documents.first {
            summary += primary.summary
            if !signals.isEmpty {
                summary += "\n\nThere are \(signals.count) recent signal(s) related to this topic, including "
                let signalTypes = Set(signals.map { $0.type.rawValue }).joined(separator: ", ")
                summary += "\(signalTypes) events."
            }
        }

        return summary
    }

    private func buildSignals(
        forDocuments documents: [KnowledgeDocument],
        tickets: [KnowledgeTicket],
        commits: [RepositoryCommit],
        workflows: [WorkflowChange],
        releases: [ReleaseNote]
    ) -> [KnowledgeSignal] {
        let docIDs = Set(documents.map { $0.id })
        var signals: [KnowledgeSignal] = []

        signals += commits.filter { !Set($0.relatedDocumentIDs).isDisjoint(with: docIDs) }
            .prefix(3)
            .map { KnowledgeSignal(
                id: $0.id, type: .commit, title: $0.message, date: $0.date,
                description: "[\($0.sha.prefix(7))] \($0.author)",
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += tickets.filter { !Set($0.relatedDocumentIDs).isDisjoint(with: docIDs) }
            .prefix(2)
            .map { KnowledgeSignal(
                id: $0.id, type: .ticket, title: $0.title, date: $0.closedDate ?? Date.now,
                description: $0.description, relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += workflows.filter { !Set($0.relatedDocumentIDs).isDisjoint(with: docIDs) }
            .prefix(2)
            .map { KnowledgeSignal(
                id: $0.id, type: .workflow, title: $0.workflowName, date: $0.changedAt,
                description: $0.description, relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        return signals.sorted { $0.date > $1.date }
    }

    private func detectIssues(documents: [KnowledgeDocument]) -> [String] {
        var issues: [String] = []
        for doc in documents {
            if doc.nextReview < Date.now {
                issues.append("'\(doc.title)' is past its review date and may contain outdated information")
            }
            if doc.confidence == .low {
                issues.append("'\(doc.title)' has low confidence — verify with the owner before relying on it")
            }
            if !doc.openAIQuestions.isEmpty {
                issues.append("'\(doc.title)' has \(doc.openAIQuestions.count) unresolved question(s)")
            }
        }
        return issues
    }

    private func suggestActions(question: String, documents: [KnowledgeDocument]) -> [String] {
        var actions: [String] = []
        if documents.isEmpty {
            actions.append("Try searching with different keywords")
            return actions
        }
        actions.append("Open the document detail to see the full content and health status")
        if documents.contains(where: { $0.nextReview < Date.now }) {
            actions.append("Generate an update proposal for documents that are past their review date")
        }
        actions.append("Check the related signals for recent changes that may affect this topic")
        return actions
    }
}
