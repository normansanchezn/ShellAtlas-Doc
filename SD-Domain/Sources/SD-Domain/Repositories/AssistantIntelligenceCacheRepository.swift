import Foundation

public struct CachedAssistantAnswer: Sendable, Equatable {
    public let answer: String
    public let sourceDocIds: [String]
    public let sourceTitles: [String]
    public let confidence: DocumentationAnswerConfidence
    public let hitCount: Int

    public init(
        answer: String,
        sourceDocIds: [String],
        sourceTitles: [String],
        confidence: DocumentationAnswerConfidence,
        hitCount: Int
    ) {
        self.answer = answer
        self.sourceDocIds = sourceDocIds
        self.sourceTitles = sourceTitles
        self.confidence = confidence
        self.hitCount = hitCount
    }

    public var sources: [DocumentationSource] {
        zip(sourceDocIds, sourceTitles).map { id, title in
            DocumentationSource(id: id, documentId: id, title: title)
        }
    }
}

public struct AssistantIntelligenceSaveRequest: Sendable {
    public let questionHash: String
    public let keywords: [String]
    public let answer: String
    public let diagramType: String?
    public let sourceDocIds: [String]
    public let sourceTitles: [String]
    public let confidence: String

    public init(
        questionHash: String,
        keywords: [String],
        answer: String,
        diagramType: String?,
        sourceDocIds: [String],
        sourceTitles: [String],
        confidence: String
    ) {
        self.questionHash = questionHash
        self.keywords = keywords
        self.answer = answer
        self.diagramType = diagramType
        self.sourceDocIds = sourceDocIds
        self.sourceTitles = sourceTitles
        self.confidence = confidence
    }
}

public protocol AssistantIntelligenceCacheRepository: Sendable {
    func findSimilarAnswer(questionHash: String, keywords: [String]) async throws -> CachedAssistantAnswer?
    func saveAnswer(_ request: AssistantIntelligenceSaveRequest) async throws
}
