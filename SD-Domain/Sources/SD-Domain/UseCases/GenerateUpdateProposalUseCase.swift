import Foundation
import DS_Core

public struct GenerateUpdateProposalUseCase: Sendable {
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

    public func execute(document: KnowledgeDocument) async throws -> UpdateProposal {
        async let tickets = ticketRepository.getTickets()
        async let commits = signalRepository.getCommits()
        async let workflows = signalRepository.getWorkflowChanges()
        async let releases = releaseRepository.getReleases()

        let (t, c, w, r) = try await (tickets, commits, workflows, releases)

        let relatedSignals = buildSignals(document: document, tickets: t, commits: c, workflows: w, releases: r)
        let reasons = buildReasons(document: document, signals: relatedSignals)
        let outdatedSections = detectOutdatedSections(document: document, signals: relatedSignals)
        let updates = generateSuggestedUpdates(document: document, signals: relatedSignals)
        let questions = buildOpenQuestions(document: document, signals: relatedSignals)

        return UpdateProposal(
            documentID: document.id,
            currentSummary: document.summary,
            relatedSignals: relatedSignals,
            reviewReasons: reasons,
            potentiallyOutdatedSections: outdatedSections,
            suggestedUpdates: updates,
            openQuestions: questions + document.openAIQuestions,
            confidenceLevel: document.confidence,
            generatedAt: Date.now
        )
    }

    private func buildSignals(
        document: KnowledgeDocument,
        tickets: [KnowledgeTicket],
        commits: [RepositoryCommit],
        workflows: [WorkflowChange],
        releases: [ReleaseNote]
    ) -> [KnowledgeSignal] {
        var signals: [KnowledgeSignal] = []

        signals += commits
            .filter { $0.relatedDocumentIDs.contains(document.id) && $0.date > document.lastValidated }
            .map { KnowledgeSignal(
                id: $0.id, type: .commit, title: $0.message, date: $0.date,
                description: "[\($0.sha.prefix(7))] \($0.author) changed: \($0.changedFiles.joined(separator: ", "))",
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += workflows
            .filter { $0.relatedDocumentIDs.contains(document.id) && $0.changedAt > document.lastValidated }
            .map { KnowledgeSignal(
                id: $0.id, type: .workflow, title: $0.workflowName, date: $0.changedAt,
                description: $0.description, relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += tickets
            .filter { $0.relatedDocumentIDs.contains(document.id) }
            .map { KnowledgeSignal(
                id: $0.id, type: .ticket, title: $0.title, date: $0.closedDate ?? Date.now,
                description: $0.description, relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        signals += releases
            .filter { $0.relatedDocumentIDs.contains(document.id) && $0.releaseDate > document.lastValidated }
            .map { KnowledgeSignal(
                id: $0.id, type: .release, title: "Release \($0.version)", date: $0.releaseDate,
                description: $0.highlights.joined(separator: "; "),
                relatedDocumentIDs: $0.relatedDocumentIDs
            )}

        return signals.sorted { $0.date > $1.date }
    }

    private func buildReasons(document: KnowledgeDocument, signals: [KnowledgeSignal]) -> [String] {
        var reasons: [String] = []
        if document.nextReview < Date.now {
            reasons.append("Document is past its scheduled review date")
        }
        let commitCount = signals.filter { $0.type == .commit }.count
        if commitCount > 0 {
            reasons.append("\(commitCount) new commit(s) may have changed related processes")
        }
        let workflowCount = signals.filter { $0.type == .workflow }.count
        if workflowCount > 0 {
            reasons.append("\(workflowCount) workflow change(s) detected since last validation")
        }
        if document.confidence == .low {
            reasons.append("Low confidence score requires review")
        }
        if !document.openAIQuestions.isEmpty {
            reasons.append("Unresolved AI questions need attention")
        }
        return reasons
    }

    private func detectOutdatedSections(document: KnowledgeDocument, signals: [KnowledgeSignal]) -> [String] {
        var sections: [String] = []
        let commitSignals = signals.filter { $0.type == .commit }

        for signal in commitSignals {
            if signal.description.localizedStandardContains("build.gradle") ||
               signal.description.localizedStandardContains("updateconfig") {
                sections.append("Version management and build configuration steps")
                break
            }
        }

        let workflowSignals = signals.filter { $0.type == .workflow }
        if !workflowSignals.isEmpty {
            sections.append("GitHub Actions workflow references and automation steps")
        }

        for signal in signals {
            if signal.description.localizedStandardContains("lokalise") ||
               signal.description.localizedStandardContains("strings") {
                sections.append("Localization and strings management section")
                break
            }
        }

        if document.nextReview < Date.now {
            sections.append("Process overview — may reflect outdated tooling or procedures")
        }

        return sections.isEmpty ? ["Review all sections for accuracy"] : sections
    }

    private func generateSuggestedUpdates(document: KnowledgeDocument, signals: [KnowledgeSignal]) -> [String] {
        var updates: [String] = []

        let hasCommitSignals = signals.contains { $0.type == .commit }
        let hasWorkflowSignals = signals.contains { $0.type == .workflow }

        if hasCommitSignals {
            updates.append("Review recent commits and update any changed steps or file references")
        }
        if hasWorkflowSignals {
            updates.append("Update GitHub Actions workflow references to reflect current workflow names and triggers")
        }
        updates.append("Validate all tool versions and configuration values are current")
        updates.append("Confirm branch naming conventions with the current sprint strategy")
        if document.aiUpdateSignals.contains(where: { $0.localizedStandardContains("pilot") }) {
            updates.append("Verify pilot branch naming convention is still extra/pilot-x.xx.x format")
        }
        if document.tags.contains("eosb") || document.tags.contains("release") {
            updates.append("Cross-check release steps against the latest GitHub Actions run")
        }
        updates.append("Update last validated date after review")

        return updates
    }

    private func buildOpenQuestions(document: KnowledgeDocument, signals: [KnowledgeSignal]) -> [String] {
        var questions: [String] = []

        if signals.contains(where: { $0.type == .workflow }) {
            questions.append("Do the GitHub Actions workflow changes affect any documented steps?")
        }
        if signals.contains(where: { $0.type == .commit }) {
            questions.append("Have any of the recent commits introduced new process requirements?")
        }
        questions.append("Is the current document owner still the right person to validate this?")
        questions.append("Are there any new tools or integrations that should be added to this document?")

        return questions
    }
}
