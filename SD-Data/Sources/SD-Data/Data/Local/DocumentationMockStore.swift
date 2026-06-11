import Foundation
import SD_Domain

actor DocumentationMockStore {
    private var documents: [DocumentationDocument]
    private let persistsChanges: Bool

    init(documents: [DocumentationDocument] = DocumentationMockData.documents, persistsChanges: Bool = true) {
        self.persistsChanges = persistsChanges
        self.documents = persistsChanges ? Self.loadPersistedDocuments() ?? documents : documents
    }

    func getDocuments() -> [DocumentationDocument] {
        documents.sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
    }

    func getDocument(id: String) throws -> DocumentationDocument {
        guard let document = documents.first(where: { $0.id == id }) else {
            throw DomainError.documentNotFound(id)
        }
        return document
    }

    func searchDocumentsByTitle(_ query: String) -> [DocumentationDocument] {
        let normalizedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedQuery.isEmpty else { return [] }

        return documents
            .filter { $0.title.localizedStandardContains(normalizedQuery) }
            .sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
    }

    func createDocument(_ document: DocumentationDocument) throws {
        guard !documents.contains(where: { $0.id == document.id }) else {
            throw DomainError.decodingFailed("A document with id '\(document.id)' already exists")
        }
        documents.append(document)
        try persistDocuments()
    }

    func updateDocument(_ document: DocumentationDocument) throws {
        guard let index = documents.firstIndex(where: { $0.id == document.id }) else {
            throw DomainError.documentNotFound(document.id)
        }
        documents[index] = document
        try persistDocuments()
    }

    func deleteDocument(id: String) throws {
        guard let index = documents.firstIndex(where: { $0.id == id }) else {
            throw DomainError.documentNotFound(id)
        }
        documents.remove(at: index)
        try persistDocuments()
    }

    func versions(for documentId: String) throws -> [DocumentationVersion] {
        let document = try getDocument(id: documentId)
        return [
            DocumentationVersion(
                id: "mock-version-\(document.id)",
                documentId: document.id,
                versionNumber: 1,
                title: document.title,
                rawMarkdown: document.content,
                contentJSON: document.contentJSON,
                contentPlaintext: document.content,
                contentHash: "mock:\(document.content.hashValue)",
                changeSummary: "Mock seed version",
                createdAt: document.attributes.lastUpdated
            )
        ]
    }

    private func persistDocuments() throws {
        guard persistsChanges else { return }

        let url = Self.persistedDocumentsURL
        try FileManager.default.createDirectory(
            at: url.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )

        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        let data = try encoder.encode(documents)
        try data.write(to: url, options: .atomic)
    }

    private static func loadPersistedDocuments() -> [DocumentationDocument]? {
        let url = persistedDocumentsURL
        guard FileManager.default.fileExists(atPath: url.path) else { return nil }

        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            return try decoder.decode([DocumentationDocument].self, from: data)
        } catch {
            return nil
        }
    }

    private static var persistedDocumentsURL: URL {
        let baseURL = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            ?? FileManager.default.temporaryDirectory
        return baseURL
            .appendingPathComponent("ShellDoc", isDirectory: true)
            .appendingPathComponent("documentation-documents.json")
    }
}

enum DocumentationMockData {
    static let sharedStore = DocumentationMockStore()

    static let documents: [DocumentationDocument] = seedDocuments + DocumentationExportMock.documents

    private static let seedDocuments: [DocumentationDocument] = [
        doc(
            id: "android-loyalty-rewards-flow",
            title: "Rewards Flow",
            summary: "Android loyalty reward enrollment, progress, and redemption behavior.",
            owner: "Maya Chen",
            module: "Loyalty",
            team: "Android Shell App",
            status: .published,
            tags: ["android", "loyalty", "rewards"],
            createdAt: "2026-01-08T14:00:00Z",
            lastUpdated: "2026-05-20T18:30:00Z",
            content: """
            # Rewards Flow

            This document explains how loyalty rewards are presented and redeemed in the Android Shell app.

            ## Entry Points

            - Home loyalty card
            - Rewards tab
            - Post-transaction confirmation

            ## Business Rules

            - Eligible rewards appear before informational tiles.
            - Expired rewards are hidden from the active list.
            - Redemption requires a valid loyalty session.
            """
        ),
        doc(
            id: "android-loyalty-in-progress-offers",
            title: "In-Progress Offers",
            summary: "Rules for displaying partially completed loyalty offers.",
            owner: "Carlos Reyes",
            module: "Loyalty",
            team: "Android Shell App",
            status: .updatesPending,
            tags: ["android", "offers", "sorting"],
            createdAt: "2026-01-12T10:15:00Z",
            lastUpdated: "2026-05-28T16:10:00Z",
            content: """
            # In-Progress Offers

            This document explains how in-progress offers are displayed in the Shell app.

            ## Business Rules

            - Offers are sorted by priority.
            - Lower priority value means higher priority.
            - If priority is missing, the offer should be placed at the end.
            - If two offers have the same priority, the newest start date should appear first.

            ## Android Notes

            The Android implementation should keep sorting logic outside the View.

            ## QA Notes

            Validate behavior with:
            - Offers with priority.
            - Offers without priority.
            - Offers with the same priority.
            - Offers with different start dates.
            """
        ),
        doc(
            id: "android-loyalty-aa-advantage",
            title: "AA Advantage",
            summary: "Android integration notes for loyalty partner benefit visibility.",
            owner: "Priya Raman",
            module: "Loyalty",
            team: "Partnerships",
            status: .draft,
            tags: ["android", "partner", "loyalty"],
            createdAt: "2026-02-01T09:00:00Z",
            lastUpdated: "2026-04-18T12:45:00Z",
            content: """
            # AA Advantage

            Partner loyalty benefits are shown when the customer profile includes a linked AA Advantage account.

            ## Display Rules

            - Show the partner card only for verified linked accounts.
            - Hide partner enrollment prompts during checkout.
            - Refresh entitlement state after profile sync.
            """
        ),
        doc(
            id: "android-transactions-receipts",
            title: "Receipts",
            summary: "Receipt presentation rules after fuel and convenience store purchases.",
            owner: "Jordan Blake",
            module: "Transactions",
            team: "Payments",
            status: .published,
            tags: ["android", "receipts", "transactions"],
            createdAt: "2026-01-20T19:00:00Z",
            lastUpdated: "2026-05-10T14:20:00Z",
            content: """
            # Receipts

            Receipts summarize completed fuel and store transactions.

            ## Required Fields

            - Site name
            - Transaction date
            - Masked payment method
            - Total paid
            - Reward savings when applicable
            """
        ),
        doc(
            id: "android-transactions-payment-summary",
            title: "Payment Summary",
            summary: "How the payment summary appears before transaction confirmation.",
            owner: "Avery Smith",
            module: "Transactions",
            team: "Payments",
            status: .published,
            tags: ["android", "payment", "checkout"],
            createdAt: "2026-02-11T15:00:00Z",
            lastUpdated: "2026-05-06T20:00:00Z",
            content: """
            # Payment Summary

            The payment summary gives customers a final review before confirming a purchase.

            ## Summary Sections

            - Product or pump details
            - Payment method
            - Loyalty savings
            - Taxes and total
            """
        ),
        doc(
            id: "android-release-end-of-sprint-build",
            title: "End of Sprint Build",
            summary: "Checklist for producing Android end-of-sprint validation builds.",
            owner: "Nora Patel",
            module: "Release Process",
            team: "Mobile Release",
            status: .updatesPending,
            tags: ["android", "release", "qa"],
            createdAt: "2026-03-05T13:30:00Z",
            lastUpdated: "2026-05-29T11:25:00Z",
            content: """
            # End of Sprint Build

            End-of-sprint builds are created for QA validation after feature freeze.

            ## Checklist

            - Confirm release branch is green.
            - Include approved feature flags.
            - Attach build notes to the QA handoff.
            - Share install instructions with stakeholders.
            """
        ),
        doc(
            id: "android-release-qa-validation",
            title: "QA Validation",
            summary: "Android release validation scope and sign-off expectations.",
            owner: "Diego Flores",
            module: "Release Process",
            team: "QA",
            status: .published,
            tags: ["android", "qa", "release"],
            createdAt: "2026-02-22T08:30:00Z",
            lastUpdated: "2026-05-14T17:45:00Z",
            content: """
            # QA Validation

            QA validates release candidates against the agreed sprint scope.

            ## Validation Areas

            - Login
            - Loyalty enrollment
            - Fuel transaction happy path
            - Receipt generation
            - Regression smoke tests
            """
        ),
        doc(
            id: "ios-authentication",
            title: "Authentication",
            summary: "iOS login, token refresh, and session recovery behavior.",
            owner: "Elena Vargas",
            module: "Authentication",
            team: "iOS Shell App",
            status: .published,
            tags: ["ios", "auth", "session"],
            createdAt: "2026-01-16T16:00:00Z",
            lastUpdated: "2026-05-24T09:10:00Z",
            content: """
            # Authentication

            The iOS authentication flow handles sign in, token refresh, and session recovery.

            ## Session Rules

            - Refresh tokens before protected requests when possible.
            - Prompt for login only after refresh failure.
            - Preserve the intended destination after re-authentication.
            """
        ),
        doc(
            id: "ios-station-locator",
            title: "Station Locator",
            summary: "iOS map and list behavior for finding Shell stations.",
            owner: "Mateo Ruiz",
            module: "Station Locator",
            team: "iOS Shell App",
            status: .updatesPending,
            tags: ["ios", "maps", "stations"],
            createdAt: "2026-03-02T10:00:00Z",
            lastUpdated: "2026-05-18T15:35:00Z",
            content: """
            # Station Locator

            Station Locator helps users find nearby Shell stations and filter services.

            ## Filters

            - Fuel type
            - Convenience store
            - Car wash
            - Loyalty participation
            """
        ),
        doc(
            id: "backend-loyalty-apis",
            title: "Loyalty APIs",
            summary: "Backend contracts used by mobile loyalty surfaces.",
            owner: "Samir Gupta",
            module: "Loyalty APIs",
            team: "Backend Platform",
            status: .published,
            tags: ["backend", "api", "loyalty"],
            createdAt: "2026-01-28T12:00:00Z",
            lastUpdated: "2026-05-22T13:15:00Z",
            content: """
            # Loyalty APIs

            Mobile clients consume loyalty APIs to load points, rewards, offers, and redemption state.

            ## Client Expectations

            - Treat missing optional fields as unavailable data.
            - Retry transient failures through the networking layer.
            - Do not cache redemption eligibility beyond the active session.
            """
        ),
        doc(
            id: "backend-token-refresh",
            title: "Token Refresh",
            summary: "Backend token refresh rules shared by iOS and Android clients.",
            owner: "Leah Thompson",
            module: "Identity",
            team: "Backend Platform",
            status: .outdated,
            tags: ["backend", "auth", "tokens"],
            createdAt: "2026-01-30T11:00:00Z",
            lastUpdated: "2026-04-10T10:30:00Z",
            content: """
            # Token Refresh

            Token refresh keeps authenticated sessions active without requiring repeated sign in.

            ## Known Gap

            This document should be reviewed against the current identity gateway behavior before release.
            """
        ),
        doc(
            id: "process-pull-request-flow",
            title: "Pull Request Flow",
            summary: "Engineering process for code review and merge readiness.",
            owner: "Alex Morgan",
            module: "Process",
            team: "Product Engineering",
            status: .published,
            tags: ["process", "review", "engineering"],
            createdAt: "2026-02-06T18:00:00Z",
            lastUpdated: "2026-05-12T16:30:00Z",
            content: """
            # Pull Request Flow

            Pull requests should be small enough to review and include test evidence.

            ## Required Before Merge

            - Linked ticket
            - Description of behavior changed
            - Screenshots for UI changes
            - Passing automated checks
            """
        ),
        doc(
            id: "process-qa-handoff",
            title: "QA Handoff",
            summary: "Expected handoff details between engineering and QA.",
            owner: "Bianca Soto",
            module: "Process",
            team: "QA",
            status: .published,
            tags: ["process", "qa", "handoff"],
            createdAt: "2026-02-09T17:00:00Z",
            lastUpdated: "2026-05-17T19:45:00Z",
            content: """
            # QA Handoff

            QA handoff records what changed, how to validate it, and what risk areas need attention.

            ## Include

            - Build number
            - Feature flags
            - Test accounts
            - Known limitations
            """
        ),
        doc(
            id: "process-release-notes",
            title: "Release Notes",
            summary: "Release note format for mobile app updates.",
            owner: "Taylor Kim",
            module: "Process",
            team: "Mobile Release",
            status: .draft,
            tags: ["process", "release", "notes"],
            createdAt: "2026-03-12T09:00:00Z",
            lastUpdated: "2026-05-04T12:00:00Z",
            content: """
            # Release Notes

            Release notes summarize user-facing changes and operational risk.

            ## Format

            - Highlights
            - Fixes
            - Known issues
            - Rollout notes
            """
        )
    ]

    static func documentationTree(for documents: [DocumentationDocument]) -> [DocumentationNode] {
        let explicitParentIDs: Set<String> = [
            "android",
            "android-loyalty",
            "android-transactions",
            "android-release-process",
            "ios",
            "backend",
            "process",
            "shell-app-documentation",
            "created-documents"
        ]
        let knownTree = DocumentationNode(
            id: "shell-app-documentation",
            title: "Shell App Documentation",
            type: .folder,
            children: [
                folder("android", "Android", [
                    folder("android-loyalty", "Loyalty", docs(documents, [
                        "android-loyalty-rewards-flow",
                        "android-loyalty-in-progress-offers",
                        "android-loyalty-aa-advantage"
                    ]) + docs(withParent: "android-loyalty", in: documents)),
                    folder("android-transactions", "Transactions", docs(documents, [
                        "android-transactions-receipts",
                        "android-transactions-payment-summary"
                    ]) + docs(withParent: "android-transactions", in: documents)),
                    folder("android-release-process", "Release Process", docs(documents, [
                        "android-release-end-of-sprint-build",
                        "android-release-qa-validation"
                    ]) + docs(withParent: "android-release-process", in: documents))
                ] + docs(withParent: "android", in: documents)),
                folder("ios", "iOS", docs(documents, [
                    "ios-authentication",
                    "ios-station-locator"
                ]) + docs(withParent: "ios", in: documents)),
                folder("backend", "Backend", docs(documents, [
                    "backend-loyalty-apis",
                    "backend-token-refresh"
                ]) + docs(withParent: "backend", in: documents)),
                folder("process", "Process", docs(documents, [
                    "process-pull-request-flow",
                    "process-qa-handoff",
                    "process-release-notes"
                ]) + docs(withParent: "process", in: documents))
            ] + docs(withParent: "shell-app-documentation", in: documents)
        )

        let knownIDs = Set(knownTree.flattenedDocumentIDs)
        let createdDocuments = documents
            .filter { document in
                guard !knownIDs.contains(document.id) else { return false }
                guard let parentFolderId = document.attributes.parentFolderId else { return true }
                return !explicitParentIDs.contains(parentFolderId) || parentFolderId == "created-documents"
            }
            .sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
            .map(documentNode)

        var rootNodes: [DocumentationNode] = [knownTree]
        if !createdDocuments.isEmpty {
            rootNodes.append(folder("created-documents", "Created Documents", createdDocuments))
        }
        return rootNodes
    }

    private static func doc(
        id: String,
        title: String,
        summary: String,
        owner: String,
        module: String,
        team: String,
        status: DocumentationStatus,
        tags: [String],
        createdAt: String,
        lastUpdated: String,
        content: String
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
                status: status,
                tags: tags,
                lastUpdated: date(lastUpdated),
                createdAt: date(createdAt)
            )
        )
    }

    private static func date(_ value: String) -> Date {
        ISO8601DateFormatter().date(from: value) ?? Date(timeIntervalSince1970: 0)
    }

    private static func folder(_ id: String, _ title: String, _ children: [DocumentationNode]) -> DocumentationNode {
        DocumentationNode(id: id, title: title, type: .folder, children: children)
    }

    private static func docs(_ documents: [DocumentationDocument], _ ids: [String]) -> [DocumentationNode] {
        ids.compactMap { id in
            documents.first(where: { $0.id == id }).map(documentNode)
        }
    }

    private static func docs(withParent parentFolderId: String, in documents: [DocumentationDocument]) -> [DocumentationNode] {
        documents
            .filter { $0.attributes.parentFolderId == parentFolderId }
            .sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
            .map(documentNode)
    }

    private static func documentNode(_ document: DocumentationDocument) -> DocumentationNode {
        documentNode(document, namespace: "node")
    }

    private static func documentNode(_ document: DocumentationDocument, namespace: String) -> DocumentationNode {
        DocumentationNode(
            id: "\(namespace)-\(document.id)",
            title: document.title,
            type: .document,
            documentId: document.id
        )
    }
}

private extension DocumentationNode {
    var flattenedDocumentIDs: [String] {
        switch type {
        case .document:
            documentId.map { [$0] } ?? []
        case .folder:
            children.flatMap(\.flattenedDocumentIDs)
        }
    }
}
