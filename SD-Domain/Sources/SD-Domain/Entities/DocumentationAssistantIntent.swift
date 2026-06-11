import Foundation

public enum DocumentationAssistantIntent: String, Equatable, Sendable {
    case definitionQuestion = "definition_question"
    case acronymQuestion = "acronym_question"
    case processFlowQuestion = "process_flow_question"
    case technicalImplementationQuestion = "technical_implementation_question"
    case troubleshootingQuestion = "troubleshooting_question"
    case releaseOrBuildQuestion = "release_or_build_question"
    case businessRuleQuestion = "business_rule_question"
    case comparisonQuestion = "comparison_question"
    case summaryRequest = "summary_request"
    case documentationImprovementRequest = "documentation_improvement_request"
    case unknown
}

public struct DocumentationAssistantIntentClassifier: Sendable {
    public init() {}

    public func classify(question: String, snippets: [DocumentationSnippet] = []) -> DocumentationAssistantIntent {
        let normalized = question.assistantPolicyNormalized
        let terms = Set(normalized.assistantPolicyTerms)
        let context = snippets
            .map { "\($0.documentTitle) \($0.sectionTitle ?? "") \($0.text)" }
            .joined(separator: " ")
            .assistantPolicyNormalized

        if containsAny(normalized, [
            "mejora", "mejorar", "actualiza", "update the doc", "improve documentation",
            "rewrite", "redacta", "draft", "suggested documentation"
        ]) {
            return .documentationImprovementRequest
        }

        if containsAny(normalized, ["compare", "comparison", "difference", "versus", " vs ", "diferencia", "comparar"]) {
            return .comparisonQuestion
        }

        if containsAny(normalized, ["troubleshoot", "troubleshooting", "error", "fails", "failure", "fix", "cause", "validate", "debug", "falla", "fallo", "causa", "validar", "solucion"]) {
            return .troubleshootingQuestion
        }

        if containsAny(normalized, ["acronym", "sigla", "significa", "meaning of", "que significa"]) {
            return .acronymQuestion
        }

        if containsKnownAcronym(terms), containsAny(normalized, ["what is", "que es", "define", "definition", "meaning"]) {
            return .acronymQuestion
        }

        if containsAny(normalized, ["release", "build", "deployment", "deploy", "qa", "uat", "prod", "rc", "sprint", "eosb", "hotfix", "rollback"]) {
            return .releaseOrBuildQuestion
        }

        if containsKnownAcronym(terms) {
            return .acronymQuestion
        }

        if containsAny(normalized, ["flow", "process", "workflow", "diagram", "sequence", "steps", "handoff", "journey", "approval", "pipeline", "flujo", "proceso", "diagrama", "pasos", "secuencia"]) {
            return .processFlowQuestion
        }

        if containsAny(context, ["## checklist", "## steps", "process", "workflow", "flowchart", "handoff", "validation"]) {
            return .processFlowQuestion
        }

        if containsAny(normalized, ["business rule", "rule", "policy", "allowed", "requires", "must", "regla", "politica", "debe", "requiere"]) {
            return .businessRuleQuestion
        }

        if containsAny(normalized, ["implement", "implementation", "api", "code", "endpoint", "class", "function", "technical", "implementar", "codigo", "endpoint"]) {
            return .technicalImplementationQuestion
        }

        if containsAny(normalized, ["summary", "summarize", "resume", "resumen", "explicame", "explain", "explica"]) {
            return .summaryRequest
        }

        if containsAny(normalized, ["what is", "what are", "define", "definition", "que es", "cual es"]) {
            return .definitionQuestion
        }

        return .unknown
    }

    private func containsAny(_ text: String, _ values: [String]) -> Bool {
        values.contains { text.contains($0) }
    }

    private func containsKnownAcronym(_ terms: Set<String>) -> Bool {
        !terms.isDisjoint(with: [
            "pi", "eosb", "eosb1", "qa", "rc", "pr", "uat", "sit", "mvp",
            "dev", "prod", "ado", "ba"
        ])
    }
}

extension String {
    var assistantPolicyNormalized: String {
        folding(options: [.caseInsensitive, .diacriticInsensitive], locale: .current)
            .lowercased()
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var assistantPolicyTerms: [String] {
        assistantPolicyNormalized
            .components(separatedBy: CharacterSet.alphanumerics.inverted)
            .filter { !$0.isEmpty }
    }
}
