import Foundation

public struct KnowledgeDocument: Identifiable, Sendable {
    public let id: String
    public let title: String
    public let type: DocumentType
    public let area: String
    public let platform: Platform
    public let status: DocumentStatus
    public let confidence: ConfidenceLevel
    public let owner: String
    public let mainContact: String
    public let branches: [String]
    public let relatedTools: [String]
    public let relatedRepositories: [String]
    public let summary: String
    public let content: String
    public let tags: [String]
    public let lastValidated: Date
    public let nextReview: Date
    public let lastUpdated: Date
    public let reviewFrequency: ReviewFrequency
    public let aiReviewPriority: AIReviewPriority
    public let relatedTicketIDs: [String]
    public let relatedCommitIDs: [String]
    public let relatedReleaseIDs: [String]
    public let relatedWorkflowIDs: [String]
    public let aiUpdateSignals: [String]
    public let openAIQuestions: [String]
    public let suggestedActions: [String]
    public let mermaidDiagram: String?
    
    public init(id: String, title: String, type: DocumentType, area: String, platform: Platform, status: DocumentStatus, confidence: ConfidenceLevel, owner: String, mainContact: String, branches: [String], relatedTools: [String], relatedRepositories: [String], summary: String, content: String, tags: [String], lastValidated: Date, nextReview: Date, lastUpdated: Date, reviewFrequency: ReviewFrequency, aiReviewPriority: AIReviewPriority, relatedTicketIDs: [String], relatedCommitIDs: [String], relatedReleaseIDs: [String], relatedWorkflowIDs: [String], aiUpdateSignals: [String], openAIQuestions: [String], suggestedActions: [String], mermaidDiagram: String?) {
        self.id = id
        self.title = title
        self.type = type
        self.area = area
        self.platform = platform
        self.status = status
        self.confidence = confidence
        self.owner = owner
        self.mainContact = mainContact
        self.branches = branches
        self.relatedTools = relatedTools
        self.relatedRepositories = relatedRepositories
        self.summary = summary
        self.content = content
        self.tags = tags
        self.lastValidated = lastValidated
        self.nextReview = nextReview
        self.lastUpdated = lastUpdated
        self.reviewFrequency = reviewFrequency
        self.aiReviewPriority = aiReviewPriority
        self.relatedTicketIDs = relatedTicketIDs
        self.relatedCommitIDs = relatedCommitIDs
        self.relatedReleaseIDs = relatedReleaseIDs
        self.relatedWorkflowIDs = relatedWorkflowIDs
        self.aiUpdateSignals = aiUpdateSignals
        self.openAIQuestions = openAIQuestions
        self.suggestedActions = suggestedActions
        self.mermaidDiagram = mermaidDiagram
    }
}
