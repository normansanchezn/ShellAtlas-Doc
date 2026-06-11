import Foundation

public enum DocumentationAssistantDefaults {
    public static let notEnoughInformationMessage = "The documentation does not contain enough information to answer that question."

    public static func notEnoughInformationMessage(for question: DocumentationQuestion) -> String {
        question.text.isLikelySpanish
            ? "La documentación no contiene suficiente información para responder esa pregunta."
            : notEnoughInformationMessage
    }
}

public struct DefaultDocumentationAssistantPromptBuilder: DocumentationAssistantPromptBuilder {
    private let intentClassifier = DocumentationAssistantIntentClassifier()

    public init() {}

    public func buildPrompt(question: String, snippets: [DocumentationSnippet]) -> String {
        let intent = intentClassifier.classify(question: question, snippets: snippets)
        let responseLanguage = question.assistantResponseLanguageInstruction
        let diagramInstruction = Self.requiresDiagram(question, snippets: snippets, intent: intent)
            ? Self.diagramInstruction(for: intent)
            : ""

        let retrievedContext = snippets.map { snippet in
            let section = snippet.sectionTitle.map { "\nSection: \($0)" } ?? ""
            return """
            Source: \(snippet.documentTitle)
            Document ID: \(snippet.documentId)\(section)
            Context:
            \(snippet.text)
            """
        }
        .joined(separator: "\n\n---\n\n")

        return """
        You are ShellDoc Assistant.

        You are an intelligent documentation assistant for internal technical, business, and process documentation.
        Your job is not only to repeat what the documents say. Your job is to help the user understand the documentation, clarify context, infer the question type, explain processes, and produce useful answers based on available documentation.

        Response language:
        - Reply in \(responseLanguage).
        - Translate every user-facing heading, fallback sentence, suggestion, note, confidence label explanation, and error-style message into the response language.
        - Do not mix English and Spanish in the final answer, except for document titles, ticket IDs, branch names, commands, file paths, code, and exact acronyms.
        - Keep document titles, ticket IDs, branch names, commands, file paths, and code exactly as written.

        Internal intent classification:
        - Detected intent: \(intent.rawValue)
        - Use this intent to choose the answer structure. Do not print the intent label unless it helps the answer.

        Rules:
        - Use the documentation context as the source of truth for ShellDoc-specific facts.
        - Prefer a useful grounded synthesis over a terse refusal.
        - If the exact answer is not explicit, explain the best supported interpretation and clearly label any gap as "Not documented" or "No document confirms this".
        - If the context truly has no relevant information, say the documentation does not contain enough information.
        - Do not invent owners, tickets, commits, release notes, QA steps, APIs, business rules, or implementation details.
        - You may expand common acronyms only when the acronym is present in or strongly supported by the context.
        - If an acronym is ambiguous, state the likely meaning and the uncertainty.
        - Mention the source document title when useful.
        - Format the answer like a modern AI assistant: short title, concise summary, structured sections, bullets or numbered steps, and a clear "Documentation gaps" section when needed.
        - Preserve code examples in fenced Markdown code blocks with the language tag when known.
        - Do not truncate the answer. Include all documented steps needed to answer the question.
        - Keep the answer clear and technical.
        - If the user asks for an update, suggest a draft change but do not modify the document automatically.
        - For yes/no, count, comparison, or "is there more than one" questions, answer from the retrieved sources and explain what the documentation proves versus what it does not prove.
        - When the context is a process or checklist, include a Mermaid diagram first even if the user did not explicitly ask for "diagram".
        - Do not dump raw chunks. Merge related chunks into one coherent answer.
        - Separate documented facts from inferred explanation.
        - Use confidence labels for acronym or uncertain answers: High, Medium, or Low.
        - When documentation is unclear, include "What the documentation says", "Clearer explanation", and "Missing or unclear details".
        - When information is missing, include "Recommended next step" instead of stopping with "I don't know".

        Common ShellDoc acronym guide:
        - EoSB / EoSB1: End of Sprint Build / End of Sprint Build 1 when used in release or QA build context.
        - PI: Program Increment when used in planning context.
        - QA: Quality Assurance.
        - ADO: Azure DevOps.
        - PR: Pull Request.
        - BA: Business Analyst.
        - UAT: User Acceptance Testing.
        - PROD: Production.
        - RC: Release Candidate.
        \(diagramInstruction)

        Answer format by intent:
        \(Self.answerTemplate(for: intent))

        Documentation Context:
        \(retrievedContext)

        User Question:
        \(question)

        Answer:
        """
    }

    private static func diagramInstruction(for intent: DocumentationAssistantIntent) -> String {
        let (diagramType, example, description) = Self.diagramSpec(for: intent)
        return """

            The retrieved documentation describes a process, flow, checklist, sequence, build, or the user is asking for one.
            Include a valid Mermaid diagram automatically. Use \(diagramType) as the diagram type — \(description). Put the diagram near the top after the short summary. Keep node labels concise and readable:

            ```mermaid
            \(example)
            ```

            After the diagram, explain the flow step by step using the provided context as the source of truth.
            Do not dump raw document content. Synthesize and summarize. Only cite exact phrases when precision matters.
            """
    }

    private static func diagramSpec(for intent: DocumentationAssistantIntent) -> (type: String, example: String, description: String) {
        switch intent {
        case .processFlowQuestion, .releaseOrBuildQuestion, .troubleshootingQuestion:
            return (
                "flowchart TD",
                "flowchart TD\n    A[First documented step] --> B[Second step]\n    B --> C[Third step]",
                "top-down flow for step sequences and release processes"
            )
        case .technicalImplementationQuestion:
            return (
                "sequenceDiagram",
                "sequenceDiagram\n    participant App\n    participant Service\n    App->>Service: Request\n    Service-->>App: Response",
                "sequence diagram for system interactions and API flows"
            )
        case .businessRuleQuestion:
            return (
                "stateDiagram-v2",
                "stateDiagram-v2\n    [*] --> Initial\n    Initial --> Valid : rule passes\n    Initial --> Invalid : rule fails\n    Valid --> [*]",
                "state diagram for rules, conditions, and decision paths"
            )
        case .comparisonQuestion:
            return (
                "flowchart LR",
                "flowchart LR\n    A[Option A] --> C{Criteria}\n    B[Option B] --> C\n    C --> D[Result]",
                "left-right flow for comparisons"
            )
        case .summaryRequest, .definitionQuestion, .acronymQuestion:
            return (
                "flowchart TD",
                "flowchart TD\n    A[Concept] --> B[Component 1]\n    A --> C[Component 2]",
                "overview diagram for definitions and summaries"
            )
        case .documentationImprovementRequest:
            return (
                "flowchart LR",
                "flowchart LR\n    A[Current state] --> B[Gap identified]\n    B --> C[Suggested improvement]",
                "improvement flow"
            )
        default:
            return (
                "flowchart TD",
                "flowchart TD\n    A[Start] --> B[Process]\n    B --> C[End]",
                "general process flow"
            )
        }
    }

    private static func requiresDiagram(
        _ question: String,
        snippets: [DocumentationSnippet],
        intent: DocumentationAssistantIntent
    ) -> Bool {
        if [
            .processFlowQuestion,
            .releaseOrBuildQuestion,
            .troubleshootingQuestion,
            .documentationImprovementRequest
        ].contains(intent) {
            return true
        }

        let normalized = question.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        if ["diagrama", "diagram", "flujo", "flow", "proceso", "process"].contains(where: { normalized.contains($0) }) {
            return true
        }

        let processSignals = [
            "process", "proceso", "flow", "flujo", "workflow", "checklist", "steps",
            "step ", "build", "handoff", "validation", "release", "qa", "sprint"
        ]
        let context = snippets
            .map { "\($0.documentTitle)\n\($0.sectionTitle ?? "")\n\($0.text)" }
            .joined(separator: "\n")
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()

        return processSignals.contains { context.contains($0) }
    }

    private static func answerTemplate(for intent: DocumentationAssistantIntent) -> String {
        switch intent {
        case .acronymQuestion:
            """
            Use this structure:
            ## Meaning
            Explain the likely meaning. If not explicitly defined, say it is inferred from context.

            ## Context
            Explain where/how the term appears in the retrieved documentation.

            ## Example
            Give a short example using only documented or clearly inferred context.

            ## Confidence
            High / Medium / Low, with one short reason.
            """
        case .processFlowQuestion, .releaseOrBuildQuestion:
            """
            Use this structure:
            ## Summary
            Give the short answer.

            ## Diagram
            Include a Mermaid flowchart.

            ## Step-by-step flow
            Number the documented steps.

            ## Notes
            Separate documented facts from inferred context.

            ## Missing or unclear details
            List gaps if the docs are incomplete.
            """
        case .troubleshootingQuestion:
            """
            Use this structure:
            ## Possible cause
            Explain the likely documented cause or supported inference.

            ## How to validate
            Number validation steps.

            ## Recommended fix
            Provide only fixes supported by the docs or clearly marked as inferred.

            ## Missing or unclear details
            List what the docs do not confirm.
            """
        case .documentationImprovementRequest:
            """
            Use this structure:
            ## What the documentation says
            Summarize the current documented content.

            ## Clearer explanation
            Rewrite the idea in clearer language.

            ## Suggested improved documentation
            Provide a draft only. Do not imply it was saved.

            ## Missing or unclear details
            List missing facts that should be added by a human.
            """
        case .comparisonQuestion:
            """
            Use this structure:
            ## Short answer
            Give the comparison result.

            ## Comparison
            Use bullets or a table.

            ## What the documentation confirms
            List confirmed facts.

            ## Missing or unclear details
            List gaps.
            """
        case .definitionQuestion, .summaryRequest, .technicalImplementationQuestion, .businessRuleQuestion, .unknown:
            """
            Use this structure:
            ## Short answer
            Answer directly.

            ## Explanation
            Clarify the documentation in user-friendly language.

            ## What the documentation confirms
            List the grounded facts.

            ## Missing or unclear details
            List gaps only if relevant.
            """
        }
    }
}

public protocol RetrieveDocumentationSnippetsUseCaseProtocol: Sendable {
    func execute(question: DocumentationQuestion, limit: Int) async throws -> [DocumentationSnippet]
}

public protocol AskDocumentationAssistantUseCaseProtocol: Sendable {
    func execute(question: DocumentationQuestion) async throws -> DocumentationAnswer
}

public protocol CheckDocumentationAssistantStatusUseCaseProtocol: Sendable {
    func execute() async -> DocumentationAssistantStatus
}

public final class RetrieveDocumentationSnippetsUseCase: RetrieveDocumentationSnippetsUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRetrievalRepository

    public init(repository: any DocumentationRetrievalRepository) {
        self.repository = repository
    }

    public func execute(question: DocumentationQuestion, limit: Int = 5) async throws -> [DocumentationSnippet] {
        let trimmedQuestion = DocumentationQuestion(text: question.text.trimmingCharacters(in: .whitespacesAndNewlines))
        guard !trimmedQuestion.text.isEmpty else { return [] }
        return try await repository.retrieveRelevantSnippets(for: trimmedQuestion, limit: max(1, limit))
    }
}

public final class CheckDocumentationAssistantStatusUseCase: CheckDocumentationAssistantStatusUseCaseProtocol, @unchecked Sendable {
    private let repository: any LocalLLMStatusRepository

    public init(repository: any LocalLLMStatusRepository) {
        self.repository = repository
    }

    public func execute() async -> DocumentationAssistantStatus {
        await repository.checkStatus()
    }
}

public final class AskDocumentationAssistantUseCase: AskDocumentationAssistantUseCaseProtocol, @unchecked Sendable {
    private let retrievalUseCase: any RetrieveDocumentationSnippetsUseCaseProtocol
    private let promptBuilder: any DocumentationAssistantPromptBuilder
    private let localLLMClient: any LocalLLMClient
    private let intelligenceCache: (any AssistantIntelligenceCacheRepository)?
    private let snippetLimit: Int

    public init(
        retrievalUseCase: any RetrieveDocumentationSnippetsUseCaseProtocol,
        promptBuilder: any DocumentationAssistantPromptBuilder,
        localLLMClient: any LocalLLMClient,
        intelligenceCache: (any AssistantIntelligenceCacheRepository)? = nil,
        snippetLimit: Int = 5
    ) {
        self.retrievalUseCase = retrievalUseCase
        self.promptBuilder = promptBuilder
        self.localLLMClient = localLLMClient
        self.intelligenceCache = intelligenceCache
        self.snippetLimit = snippetLimit
    }

    public func execute(question: DocumentationQuestion) async throws -> DocumentationAnswer {
        let trimmedQuestion = DocumentationQuestion(text: question.text.trimmingCharacters(in: .whitespacesAndNewlines))
        guard !trimmedQuestion.text.isEmpty else {
            return DocumentationAnswer(
                text: DocumentationAssistantDefaults.notEnoughInformationMessage(for: trimmedQuestion),
                sources: [],
                confidence: .notEnoughInformation
            )
        }

        let hash = Self.questionHash(trimmedQuestion.text)
        let keywords = Self.extractKeywords(trimmedQuestion.text)

        if let cache = intelligenceCache,
           let cached = try? await cache.findSimilarAnswer(questionHash: hash, keywords: keywords) {
            return DocumentationAnswer(
                text: cached.answer,
                sources: cached.sources,
                confidence: cached.confidence
            )
        }

        let snippets = try await retrievalUseCase.execute(question: trimmedQuestion, limit: snippetLimit)
        guard !snippets.isEmpty else {
            return DocumentationAnswer(
                text: DocumentationAssistantDefaults.notEnoughInformationMessage(for: trimmedQuestion),
                sources: [],
                confidence: .notEnoughInformation
            )
        }

        let prompt = promptBuilder.buildPrompt(question: trimmedQuestion.text, snippets: snippets)
        let response = try await localLLMClient.generateAnswer(prompt: prompt).trimmingCharacters(in: .whitespacesAndNewlines)
        guard !response.isEmpty else {
            return DocumentationAnswer(
                text: DocumentationAssistantDefaults.notEnoughInformationMessage(for: trimmedQuestion),
                sources: sources(from: snippets),
                confidence: .notEnoughInformation
            )
        }

        let derivedSources = sources(from: snippets)
        let answer = DocumentationAnswer(text: response, sources: derivedSources, confidence: .medium)

        if let cache = intelligenceCache {
            let saveRequest = AssistantIntelligenceSaveRequest(
                questionHash: hash,
                keywords: keywords,
                answer: response,
                diagramType: Self.detectDiagramType(in: response),
                sourceDocIds: derivedSources.map(\.documentId),
                sourceTitles: derivedSources.map(\.title),
                confidence: "medium"
            )
            Task { try? await cache.saveAnswer(saveRequest) }
        }

        return answer
    }

    private func sources(from snippets: [DocumentationSnippet]) -> [DocumentationSource] {
        var seenDocumentIds = Set<String>()
        return snippets.compactMap { snippet in
            guard !seenDocumentIds.contains(snippet.documentId) else { return nil }
            seenDocumentIds.insert(snippet.documentId)
            return DocumentationSource(
                id: snippet.id,
                documentId: snippet.documentId,
                title: snippet.documentTitle,
                sectionTitle: snippet.sectionTitle
            )
        }
    }

    private static func questionHash(_ question: String) -> String {
        let normalized = question.assistantPolicyNormalized
        var hash: UInt64 = 5381
        for scalar in normalized.unicodeScalars {
            hash = hash &* 31 &+ UInt64(scalar.value)
        }
        return String(format: "%016llx", hash)
    }

    private static func extractKeywords(_ question: String) -> [String] {
        let stopWords: Set<String> = [
            "the", "and", "for", "are", "was", "has", "have", "this", "that", "with",
            "el", "la", "los", "las", "un", "una", "es", "son", "que", "como",
            "por", "para", "en", "de", "del", "hay", "mas", "una", "del"
        ]
        return Array(
            question.assistantPolicyTerms
                .filter { $0.count > 2 && !stopWords.contains($0) }
                .prefix(12)
        )
    }

    private static func detectDiagramType(in response: String) -> String? {
        let lower = response.lowercased()
        guard lower.contains("```mermaid") else { return nil }
        if lower.contains("sequencediagram") { return "sequenceDiagram" }
        if lower.contains("statediagram") { return "stateDiagram-v2" }
        if lower.contains("gantt") { return "gantt" }
        if lower.contains("pie") { return "pie" }
        if lower.contains("flowchart lr") || lower.contains("graph lr") { return "flowchart LR" }
        return "flowchart TD"
    }
}

private extension String {
    var isLikelySpanish: Bool {
        let normalized = folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()
        let terms = [
            "que", "cual", "cuantos", "cuantas", "hay", "mas", "deberia", "proceso",
            "flujo", "diagrama", "pregunta", "documentacion", "respuesta", "como"
        ]
        return contains("¿") || contains("¡") || terms.contains { normalized.components(separatedBy: CharacterSet.alphanumerics.inverted).contains($0) }
    }

    var assistantResponseLanguageInstruction: String {
        let normalized = folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()

        if normalized.contains("answer in spanish")
            || normalized.contains("respond in spanish")
            || normalized.contains("responde en espanol")
            || normalized.contains("responder en espanol") {
            return "Spanish because the user explicitly requested it"
        }

        if normalized.contains("answer in english")
            || normalized.contains("respond in english")
            || normalized.contains("responde en ingles")
            || normalized.contains("responder en ingles") {
            return "English because the user explicitly requested it"
        }

        return isLikelySpanish ? "Spanish" : "the same language as the user question"
    }
}
