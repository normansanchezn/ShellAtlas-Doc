import Foundation
import DS_Core

public struct AssistantAnswer: Sendable {
    public let summary: String
    public let relevantDocuments: [KnowledgeDocument]
    public let relatedSignals: [KnowledgeSignal]
    public let potentialIssues: [String]
    public let suggestedActions: [String]

    public init(
        summary: String,
        relevantDocuments: [KnowledgeDocument],
        relatedSignals: [KnowledgeSignal],
        potentialIssues: [String],
        suggestedActions: [String]
    ) {
        self.summary = summary
        self.relevantDocuments = relevantDocuments
        self.relatedSignals = relatedSignals
        self.potentialIssues = potentialIssues
        self.suggestedActions = suggestedActions
    }
}
