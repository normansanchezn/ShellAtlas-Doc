import Foundation
import SD_Domain

public struct LocalDocumentationRetrievalRepository: DocumentationRetrievalRepository {
    private let documentationRepository: any DocumentationRepository

    public init(documentationRepository: any DocumentationRepository) {
        self.documentationRepository = documentationRepository
    }

    public func retrieveRelevantSnippets(
        for question: DocumentationQuestion,
        limit: Int
    ) async throws -> [DocumentationSnippet] {
        let documents: [DocumentationDocument]
        do {
            documents = try await documentationRepository.getDocuments()
        } catch {
            return []
        }
        let normalizedQuestion = question.text.normalizedForAssistantSearch.expandedAssistantAcronyms
        let questionTerms = Set(normalizedQuestion.assistantSearchTerms.expandedAssistantSearchTerms)
        guard !questionTerms.isEmpty else { return [] }

        var snippets = documents
            .compactMap { snippet(for: $0, normalizedQuestion: normalizedQuestion, questionTerms: questionTerms) }
            .sorted {
                if $0.score == $1.score {
                    return $0.documentTitle.localizedStandardCompare($1.documentTitle) == .orderedAscending
                }
                return $0.score > $1.score
            }

        if let glossarySnippet = glossarySnippet(normalizedQuestion: normalizedQuestion, questionTerms: questionTerms) {
            snippets.insert(glossarySnippet, at: 0)
        }

        return snippets
            .prefix(max(1, limit))
            .map { $0 }
    }

    private func snippet(
        for document: DocumentationDocument,
        normalizedQuestion: String,
        questionTerms: Set<String>
    ) -> DocumentationSnippet? {
        let attributes = document.attributes
        let title = document.title.normalizedForAssistantSearch
        let summary = document.summary.normalizedForAssistantSearch
        let content = document.content.normalizedForAssistantSearch
        let module = attributes.module.normalizedForAssistantSearch
        let team = attributes.team.normalizedForAssistantSearch
        let owner = attributes.owner.normalizedForAssistantSearch
        let status = attributes.status.displayName.normalizedForAssistantSearch
        let tags = attributes.tags.map(\.normalizedForAssistantSearch)

        var score = 0.0

        if title == normalizedQuestion {
            score += 50
        } else if title.contains(normalizedQuestion) || normalizedQuestion.contains(title) || intersects(title, questionTerms) {
            score += 35
        }

        if tags.contains(where: { normalizedTag in
            questionTerms.contains(normalizedTag) || intersects(normalizedTag, questionTerms)
        }) {
            score += 30
        }

        if module == normalizedQuestion || questionTerms.contains(module) || intersects(module, questionTerms) {
            score += 20
        }

        if summary.contains(normalizedQuestion) || intersects(summary, questionTerms) {
            score += 15
        }

        if content.contains(normalizedQuestion) || intersects(content, questionTerms) {
            score += 10
        }

        if owner == normalizedQuestion || team == normalizedQuestion || intersects(owner, questionTerms) || intersects(team, questionTerms) {
            score += 5
        }

        if status == normalizedQuestion || intersects(status, questionTerms) {
            score += 5
        }

        guard score > 0 else { return nil }

        return DocumentationSnippet(
            id: "\(document.id)-snippet",
            documentId: document.id,
            documentTitle: document.title,
            text: snippetText(for: document),
            score: score,
            sectionTitle: nil
        )
    }

    private func snippetText(for document: DocumentationDocument) -> String {
        let maxContentLength = 1_800
        let content = document.content.count > maxContentLength
            ? String(document.content.prefix(maxContentLength)) + "..."
            : document.content

        return """
        Summary: \(document.summary)
        Owner: \(document.attributes.owner)
        Module: \(document.attributes.module)
        Team: \(document.attributes.team)
        Status: \(document.attributes.status.displayName)
        Tags: \(document.attributes.tags.joined(separator: ", "))

        \(content)
        """
    }

    private func intersects(_ searchableText: String, _ questionTerms: Set<String>) -> Bool {
        !questionTerms.isDisjoint(with: searchableText.assistantSearchTerms)
    }

    private func glossarySnippet(normalizedQuestion: String, questionTerms: Set<String>) -> DocumentationSnippet? {
        let glossaryTerms = InternalAcronymGlossary.terms
        let exactQuestionTerms = Set(normalizedQuestion.assistantAllTerms)
        let matchedTerms = glossaryTerms.filter { term in
            questionTerms.contains(term.key) || exactQuestionTerms.contains(term.key)
        }

        guard !matchedTerms.isEmpty else { return nil }

        let entries = matchedTerms
            .sorted { $0.key < $1.key }
            .map(\.value)
            .joined(separator: "\n\n---\n\n")

        return DocumentationSnippet(
            id: "internal-acronyms-glossary-snippet",
            documentId: "internal-acronyms-glossary",
            documentTitle: "Internal Acronyms Glossary",
            text: entries,
            score: 42,
            sectionTitle: "Acronyms"
        )
    }
}

private enum InternalAcronymGlossary {
    static let terms: [String: String] = [
        "eosb": """
        ## EoSB
        Possible meaning: End of Sprint Build.
        Context: Used in sprint-end release, QA validation, build handoff, and release candidate documentation.
        Confidence: Medium unless a source document defines it directly.
        """,
        "eosb1": """
        ## EoSB1
        Possible meaning: End of Sprint Build 1.
        Context: Used for the first end-of-sprint validation build prepared for QA review.
        Confidence: Medium unless a source document defines it directly.
        """,
        "pi": """
        ## PI
        Possible meanings: Program Increment or Product Increment.
        Context: Used in planning, delivery, product, or release documentation.
        Confidence: Medium unless the specific document defines it directly.
        """,
        "qa": """
        ## QA
        Possible meaning: Quality Assurance.
        Context: Used in validation, regression, smoke testing, sign-off, and handoff documentation.
        Confidence: High.
        """,
        "rc": """
        ## RC
        Possible meaning: Release Candidate.
        Context: Used in release, validation, and deployment documentation.
        Confidence: Medium unless a source document defines it directly.
        """,
        "pr": """
        ## PR
        Possible meaning: Pull Request.
        Context: Used in code review, merge readiness, and engineering workflow documentation.
        Confidence: High in engineering documentation.
        """,
        "uat": """
        ## UAT
        Possible meaning: User Acceptance Testing.
        Context: Used in validation, release, business acceptance, and environment documentation.
        Confidence: High in release documentation.
        """,
        "sit": """
        ## SIT
        Possible meaning: System Integration Testing.
        Context: Used in integration validation, QA, and release readiness documentation.
        Confidence: Medium unless a source document defines it directly.
        """,
        "mvp": """
        ## MVP
        Possible meaning: Minimum Viable Product.
        Context: Used in product delivery, scope, and planning documentation.
        Confidence: High in product documentation.
        """,
        "dev": """
        ## Dev
        Possible meaning: Development environment or developer, depending on context.
        Context: Used in environment, build, deployment, and team workflow documentation.
        Confidence: Medium because the meaning depends on context.
        """,
        "prod": """
        ## Prod
        Possible meaning: Production environment.
        Context: Used in release, deployment, environment, and production support documentation.
        Confidence: High in release documentation.
        """,
        "ado": """
        ## ADO
        Possible meaning: Azure DevOps.
        Context: Used for tickets, work items, release notes, QA defects, and planning boards.
        Confidence: High in ShellDoc release and ticket documentation.
        """
    ]
}

private extension String {
    var normalizedForAssistantSearch: String {
        folding(options: [.caseInsensitive, .diacriticInsensitive], locale: .current)
            .lowercased()
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var assistantSearchTerms: [String] {
        normalizedForAssistantSearch
            .components(separatedBy: CharacterSet.alphanumerics.inverted)
            .filter { $0.count > 2 }
    }

    var assistantAllTerms: [String] {
        normalizedForAssistantSearch
            .components(separatedBy: CharacterSet.alphanumerics.inverted)
            .filter { !$0.isEmpty }
    }

    var expandedAssistantAcronyms: String {
        var expanded = " \(self) "
        let exactTerms = Set(assistantAllTerms)
        let replacements: [(String, String)] = [
            ("eosb1", "eosb1 eosb end of sprint build end sprint build release candidate qa validation"),
            ("eosb", "eosb end of sprint build end sprint build release candidate qa validation"),
            ("end sprint", "end of sprint"),
            ("sprint build", "end of sprint build"),
            ("pi", "pi program increment planning"),
            ("ado", "ado azure devops ticket work item"),
            ("pr", "pr pull request code review"),
            ("qa", "qa quality assurance validation testing"),
            ("uat", "uat user acceptance testing validation"),
            ("prod", "prod production release"),
            ("rc", "rc release candidate")
        ]

        for (term, replacement) in replacements {
            if exactTerms.contains(term) {
                expanded += " \(replacement)"
            }
        }

        return expanded.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

private extension Array where Element == String {
    var expandedAssistantSearchTerms: [String] {
        var terms = self
        for term in self {
            terms.append(contentsOf: term.expandedAssistantAcronyms.assistantSearchTerms)
        }
        return Array(Set(terms))
    }
}
