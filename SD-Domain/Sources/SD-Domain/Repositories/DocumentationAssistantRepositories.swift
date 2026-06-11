import Foundation

public protocol DocumentationRetrievalRepository: Sendable {
    func retrieveRelevantSnippets(
        for question: DocumentationQuestion,
        limit: Int
    ) async throws -> [DocumentationSnippet]
}

public protocol DocumentationAssistantRepository: Sendable {
    func ask(question: DocumentationQuestion) async throws -> DocumentationAnswer
}

public protocol LocalLLMClient: Sendable {
    func generateAnswer(prompt: String) async throws -> String
}

public protocol LocalLLMStatusRepository: Sendable {
    func checkStatus() async -> DocumentationAssistantStatus
}

public protocol DocumentationAssistantPromptBuilder: Sendable {
    func buildPrompt(
        question: String,
        snippets: [DocumentationSnippet]
    ) -> String
}
