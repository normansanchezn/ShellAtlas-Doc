import Foundation

public struct DocumentationQuestion: Equatable, Sendable {
    public let text: String

    public init(text: String) {
        self.text = text
    }
}

public struct DocumentationAnswer: Equatable, Sendable {
    public let text: String
    public let sources: [DocumentationSource]
    public let confidence: DocumentationAnswerConfidence

    public init(
        text: String,
        sources: [DocumentationSource],
        confidence: DocumentationAnswerConfidence
    ) {
        self.text = text
        self.sources = sources
        self.confidence = confidence
    }
}

public enum DocumentationAnswerConfidence: String, Equatable, Sendable {
    case high
    case medium
    case low
    case notEnoughInformation
}

public struct DocumentationSnippet: Identifiable, Equatable, Sendable {
    public let id: String
    public let documentId: String
    public let documentTitle: String
    public let text: String
    public let score: Double
    public let sectionTitle: String?

    public init(
        id: String,
        documentId: String,
        documentTitle: String,
        text: String,
        score: Double,
        sectionTitle: String? = nil
    ) {
        self.id = id
        self.documentId = documentId
        self.documentTitle = documentTitle
        self.text = text
        self.score = score
        self.sectionTitle = sectionTitle
    }
}

public struct DocumentationSource: Identifiable, Equatable, Sendable {
    public let id: String
    public let documentId: String
    public let title: String
    public let sectionTitle: String?

    public init(
        id: String,
        documentId: String,
        title: String,
        sectionTitle: String? = nil
    ) {
        self.id = id
        self.documentId = documentId
        self.title = title
        self.sectionTitle = sectionTitle
    }
}

public struct AssistantMessage: Identifiable, Equatable, Sendable {
    public let id: String
    public let role: AssistantMessageRole
    public let text: String
    public let sources: [DocumentationSource]
    public let createdAt: Date

    public init(
        id: String = UUID().uuidString,
        role: AssistantMessageRole,
        text: String,
        sources: [DocumentationSource] = [],
        createdAt: Date = Date()
    ) {
        self.id = id
        self.role = role
        self.text = text
        self.sources = sources
        self.createdAt = createdAt
    }
}

public enum AssistantMessageRole: Equatable, Sendable {
    case user
    case assistant
    case system
}

public struct DocumentationAssistantStatus: Equatable, Sendable {
    public let isOllamaRunning: Bool
    public let isModelInstalled: Bool
    public let modelName: String
    public let message: String

    public init(
        isOllamaRunning: Bool,
        isModelInstalled: Bool,
        modelName: String,
        message: String
    ) {
        self.isOllamaRunning = isOllamaRunning
        self.isModelInstalled = isModelInstalled
        self.modelName = modelName
        self.message = message
    }

    public var isReady: Bool {
        isOllamaRunning && isModelInstalled
    }

    public var availabilityState: AssistantAvailabilityState {
        guard isOllamaRunning else { return .ollamaNotRunning }
        guard isModelInstalled else { return .modelNotInstalled(model: modelName) }
        return .available(model: modelName)
    }
}

public enum AssistantAvailabilityState: Equatable, Sendable {
    case checking
    case available(model: String)
    case ollamaNotRunning
    case modelNotInstalled(model: String)
    case unavailable(reason: String)
}
