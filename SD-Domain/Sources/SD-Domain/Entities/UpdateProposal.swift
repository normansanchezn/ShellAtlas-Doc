import Foundation
import DS_Core

public struct UpdateProposal: Sendable {
    public let documentID: String
    public let currentSummary: String
    public let relatedSignals: [KnowledgeSignal]
    public let reviewReasons: [String]
    public let potentiallyOutdatedSections: [String]
    public let suggestedUpdates: [String]
    public let openQuestions: [String]
    public let confidenceLevel: ConfidenceLevel
    public let generatedAt: Date

    public init(
        documentID: String,
        currentSummary: String,
        relatedSignals: [KnowledgeSignal],
        reviewReasons: [String],
        potentiallyOutdatedSections: [String],
        suggestedUpdates: [String],
        openQuestions: [String],
        confidenceLevel: ConfidenceLevel,
        generatedAt: Date
    ) {
        self.documentID = documentID
        self.currentSummary = currentSummary
        self.relatedSignals = relatedSignals
        self.reviewReasons = reviewReasons
        self.potentiallyOutdatedSections = potentiallyOutdatedSections
        self.suggestedUpdates = suggestedUpdates
        self.openQuestions = openQuestions
        self.confidenceLevel = confidenceLevel
        self.generatedAt = generatedAt
    }
}
