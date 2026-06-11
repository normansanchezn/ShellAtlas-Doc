import Foundation
import SD_Domain

public enum KnowledgeDocumentMapper {
    static func toDomain(_ dto: KnowledgeDocumentDTO) -> KnowledgeDocument {
        KnowledgeDocument(
            id: dto.id,
            title: dto.title,
            type: DocumentType(rawValue: dto.type) ?? .reference,
            area: dto.area,
            platform: Platform(rawValue: dto.platform) ?? .crossPlatform,
            status: DocumentStatus(rawValue: dto.status) ?? .active,
            confidence: ConfidenceLevel(rawValue: dto.confidence) ?? .medium,
            owner: dto.owner,
            mainContact: dto.mainContact,
            branches: dto.branches,
            relatedTools: dto.relatedTools,
            relatedRepositories: dto.relatedRepositories,
            summary: dto.summary,
            content: dto.content,
            tags: dto.tags,
            lastValidated: dto.lastValidated,
            nextReview: dto.nextReview,
            lastUpdated: dto.lastUpdated,
            reviewFrequency: ReviewFrequency(rawValue: dto.reviewFrequency) ?? .quarterly,
            aiReviewPriority: AIReviewPriority(rawValue: dto.aiReviewPriority) ?? .medium,
            relatedTicketIDs: dto.relatedTicketIDs,
            relatedCommitIDs: dto.relatedCommitIDs,
            relatedReleaseIDs: dto.relatedReleaseIDs,
            relatedWorkflowIDs: dto.relatedWorkflowIDs,
            aiUpdateSignals: dto.aiUpdateSignals,
            openAIQuestions: dto.openAIQuestions,
            suggestedActions: dto.suggestedActions,
            mermaidDiagram: dto.mermaidDiagram
        )
    }
}
