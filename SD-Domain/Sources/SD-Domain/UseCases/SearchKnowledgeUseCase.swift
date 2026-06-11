import Foundation

public struct SearchKnowledgeUseCase: Sendable {
    let repository: any KnowledgeDocumentRepository

    public init(repository: any KnowledgeDocumentRepository) {
        self.repository = repository
    }

    private static let aliases: [String: [String]] = [
        "release build": ["eosb", "build generation", "versioncodes", "github actions", "qa handoff", "eosb1"],
        "pilot branch": ["extra/pilot", "madf/pilot", "qa smoke test", "pilot"],
        "localization": ["lokalise", "strings.xml", "translations", "l10n", "i18n"],
        "secrets": ["azure secrets", "keychain", "environment values", "credentials"],
        "auth": ["authentication", "login", "token", "oauth", "keychain"],
        "android release": ["eosb", "build", "apk", "versioncode"],
        "deep link": ["branch", "deeplinking", "uri", "intent"],
        "rewards": ["loyalty", "points", "redemption", "shell go+"]
    ]

    public func execute(query: String) async throws -> [KnowledgeDocument] {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return [] }
        let documents = try await repository.getDocuments()
        return search(documents: documents, query: query)
    }

    public func search(documents: [KnowledgeDocument], query: String) -> [KnowledgeDocument] {
        let lowercased = query.lowercased()
        let terms = expandQuery(lowercased)
        return documents.filter { doc in
            terms.contains { term in
                doc.title.localizedStandardContains(term) ||
                doc.summary.localizedStandardContains(term) ||
                doc.content.localizedStandardContains(term) ||
                doc.tags.contains { $0.localizedStandardContains(term) } ||
                doc.platform.rawValue.localizedStandardContains(term) ||
                doc.type.rawValue.localizedStandardContains(term) ||
                doc.area.localizedStandardContains(term) ||
                doc.relatedTools.contains { $0.localizedStandardContains(term) } ||
                doc.branches.contains { $0.localizedStandardContains(term) } ||
                doc.aiUpdateSignals.contains { $0.localizedStandardContains(term) }
            }
        }
    }

    private func expandQuery(_ query: String) -> [String] {
        var terms = [query]
        for (key, values) in Self.aliases {
            if query.localizedStandardContains(key) || key.localizedStandardContains(query) {
                terms.append(contentsOf: values)
            }
        }
        return terms
    }
}
