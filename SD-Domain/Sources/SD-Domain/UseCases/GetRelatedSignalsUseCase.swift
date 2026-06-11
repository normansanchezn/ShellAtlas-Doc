import Foundation
import DS_Core

public struct GetRelatedSignalsUseCase: Sendable {
    let ticketRepository: any TicketRepository
    let signalRepository: any RepositorySignalRepository
    let releaseRepository: any ReleaseRepository

    public init(
        ticketRepository: any TicketRepository,
        signalRepository: any RepositorySignalRepository,
        releaseRepository: any ReleaseRepository
    ) {
        self.ticketRepository = ticketRepository
        self.signalRepository = signalRepository
        self.releaseRepository = releaseRepository
    }

    public func execute(for document: KnowledgeDocument) async throws -> [KnowledgeSignal] {
        async let tickets = ticketRepository.getTickets()
        async let commits = signalRepository.getCommits()
        async let workflows = signalRepository.getWorkflowChanges()
        async let releases = releaseRepository.getReleases()

        let (t, c, w, r) = try await (tickets, commits, workflows, releases)

        var signals: [KnowledgeSignal] = []

        signals += t
            .filter { $0.relatedDocumentIDs.contains(document.id) }
            .map { KnowledgeSignal(
                id: $0.id,
                type: .ticket,
                title: $0.title,
                date: $0.closedDate ?? Date.now,
                description: $0.description,
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += c
            .filter { $0.relatedDocumentIDs.contains(document.id) }
            .map { KnowledgeSignal(
                id: $0.id,
                type: .commit,
                title: $0.message,
                date: $0.date,
                description: "[\($0.sha.prefix(7))] \($0.author) — \($0.repository)",
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += r
            .filter { $0.relatedDocumentIDs.contains(document.id) }
            .map { KnowledgeSignal(
                id: $0.id,
                type: .release,
                title: "Release \($0.version)",
                date: $0.releaseDate,
                description: $0.highlights.first ?? "",
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += w
            .filter { $0.relatedDocumentIDs.contains(document.id) }
            .map { KnowledgeSignal(
                id: $0.id,
                type: .workflow,
                title: $0.workflowName,
                date: $0.changedAt,
                description: $0.description,
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        return signals.sorted { $0.date > $1.date }
    }
}
