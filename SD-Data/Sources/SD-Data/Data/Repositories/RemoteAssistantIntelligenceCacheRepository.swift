import Foundation
import SD_Domain

private struct AssistantIntelligenceQueryResponse: Decodable, Sendable {
    let hit: Bool
    let entry: AssistantIntelligenceEntryDTO?
}

private struct AssistantIntelligenceEntryDTO: Decodable, Sendable {
    let questionHash: String
    let keywords: [String]
    let cachedAnswer: String
    let diagramType: String?
    let sourceDocIds: [String]
    let sourceTitles: [String]
    let hitCount: Int
    let confidence: String
}

private struct AssistantIntelligenceSaveRequestBody: Encodable, Sendable {
    let questionHash: String
    let keywords: [String]
    let cachedAnswer: String
    let diagramType: String?
    let sourceDocIds: [String]
    let sourceTitles: [String]
    let confidence: String
}

public struct RemoteAssistantIntelligenceCacheRepository: AssistantIntelligenceCacheRepository {
    private let client: any ShellDocAPIClientProtocol

    public init(client: any ShellDocAPIClientProtocol) {
        self.client = client
    }

    public func findSimilarAnswer(questionHash: String, keywords: [String]) async throws -> CachedAssistantAnswer? {
        let keywordsCSV = keywords.joined(separator: ",")
        let response = try await client.request(
            "/v1/assistant/intelligence",
            method: "GET",
            queryItems: [
                URLQueryItem(name: "hash", value: questionHash),
                URLQueryItem(name: "keywords", value: keywordsCSV)
            ],
            body: Optional<String>.none,
            responseType: AssistantIntelligenceQueryResponse.self
        )
        guard response.hit, let entry = response.entry else { return nil }
        let confidence: DocumentationAnswerConfidence = switch entry.confidence {
        case "high": .high
        case "low": .low
        default: .medium
        }
        return CachedAssistantAnswer(
            answer: entry.cachedAnswer,
            sourceDocIds: entry.sourceDocIds,
            sourceTitles: entry.sourceTitles,
            confidence: confidence,
            hitCount: entry.hitCount
        )
    }

    public func saveAnswer(_ request: AssistantIntelligenceSaveRequest) async throws {
        let body = AssistantIntelligenceSaveRequestBody(
            questionHash: request.questionHash,
            keywords: request.keywords,
            cachedAnswer: request.answer,
            diagramType: request.diagramType,
            sourceDocIds: request.sourceDocIds,
            sourceTitles: request.sourceTitles,
            confidence: request.confidence
        )
        try await client.requestNoContent("/v1/assistant/intelligence", method: "POST", body: body)
    }
}
