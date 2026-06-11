import Foundation
import DS_Core

public struct EvaluateDocumentHealthUseCase: Sendable {
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

    public func execute(document: KnowledgeDocument) async throws -> DocumentHealthResult {
        async let tickets = ticketRepository.getTickets()
        async let commits = signalRepository.getCommits()
        async let workflows = signalRepository.getWorkflowChanges()
        async let releases = releaseRepository.getReleases()

        let (t, c, w, r) = try await (tickets, commits, workflows, releases)
        return evaluate(document: document, tickets: t, commits: c, workflows: w, releases: r)
    }

    private func evaluate(
        document: KnowledgeDocument,
        tickets: [KnowledgeTicket],
        commits: [RepositoryCommit],
        workflows: [WorkflowChange],
        releases: [ReleaseNote]
    ) -> DocumentHealthResult {
        var score = 100
        var reasons: [String] = []
        var actions: [String] = []
        var signals: [KnowledgeSignal] = []

        let now = Date.now

        if document.nextReview < now {
            score -= 20
            reasons.append("Next review date has passed (\(formatted(document.nextReview)))")
            actions.append("Schedule document review immediately")
        }

        let daysSince = Calendar.current.dateComponents([.day], from: document.lastValidated, to: now).day ?? 0
        if daysSince > document.reviewFrequency.days {
            score -= 15
            reasons.append("Not validated in \(daysSince) days (review cycle: \(document.reviewFrequency.days) days)")
            actions.append("Validate document content against current process")
        }

        let relatedCommits = commits.filter { $0.relatedDocumentIDs.contains(document.id) && $0.date > document.lastValidated }
        let signalCommits = relatedCommits.filter { commit in
            commit.changedFiles.contains { file in
                document.aiUpdateSignals.contains { signal in
                    signal.localizedStandardContains(file) || file.localizedStandardContains(signal)
                }
            }
        }
        if !signalCommits.isEmpty {
            score -= 15
            reasons.append("\(signalCommits.count) commit(s) modified files tracked by AI update signals")
            actions.append("Review recent commits to check for process changes")
            signals += signalCommits.map { KnowledgeSignal(
                id: $0.id, type: .commit, title: $0.message,
                date: $0.date, description: "[\($0.sha.prefix(7))] \($0.author)",
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}
        }

        let relatedTickets = tickets.filter {
            $0.relatedDocumentIDs.contains(document.id) &&
            ($0.status == .closed || $0.status == .resolved) &&
            ($0.closedDate.map { $0 > document.lastValidated } ?? false)
        }
        if !relatedTickets.isEmpty {
            score -= 10
            reasons.append("\(relatedTickets.count) related ticket(s) closed after last validation")
            actions.append("Review closed tickets for process changes affecting this document")
        }

        let relatedWorkflows = workflows.filter {
            $0.relatedDocumentIDs.contains(document.id) && $0.changedAt > document.lastValidated
        }
        if !relatedWorkflows.isEmpty {
            score -= 10
            reasons.append("\(relatedWorkflows.count) GitHub Actions workflow(s) changed after last validation")
            actions.append("Verify workflow changes are reflected in document")
            signals += relatedWorkflows.map { KnowledgeSignal(
                id: $0.id, type: .workflow, title: $0.workflowName,
                date: $0.changedAt, description: $0.description,
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}
        }

        let relatedReleases = releases.filter {
            $0.relatedDocumentIDs.contains(document.id) &&
            $0.releaseDate > document.lastValidated &&
            $0.keywords.contains { kw in document.title.localizedStandardContains(kw) || document.summary.localizedStandardContains(kw) }
        }
        if !relatedReleases.isEmpty {
            score -= 5
            reasons.append("Release notes mention keywords from this document")
        }

        if document.confidence == .low {
            score -= 10
            reasons.append("Document confidence is low")
            actions.append("Increase document confidence by validating with the owner")
        }

        if document.owner.isEmpty {
            score -= 10
            reasons.append("No document owner assigned")
            actions.append("Assign a document owner")
        }

        if document.branches.isEmpty {
            score -= 5
            reasons.append("No related branches specified")
        }

        if !document.openAIQuestions.isEmpty {
            score -= 5
            reasons.append("\(document.openAIQuestions.count) unresolved AI question(s)")
            actions.append("Address open AI questions about this document")
        }

        let finalScore = max(0, score)
        let recommendation: DocumentHealthRecommendation = switch finalScore {
        case 80...100: .upToDate
        case 60..<80: .reviewRecommended
        case 40..<60: .reviewRequired
        default: .criticalReview
        }

        return DocumentHealthResult(
            healthScore: finalScore,
            recommendation: recommendation,
            reasons: reasons,
            matchedSignals: signals,
            suggestedActions: actions
        )
    }

    private func formatted(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }
}
