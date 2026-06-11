import Foundation
import SD_Domain

enum WorkflowChangeMapper {
    static func toDomain(_ dto: WorkflowChangeDTO) -> WorkflowChange {
        WorkflowChange(
            id: dto.id,
            workflowName: dto.workflowName,
            changedAt: dto.changedAt,
            description: dto.description,
            changedFiles: dto.changedFiles,
            relatedDocumentIDs: dto.relatedDocumentIDs
        )
    }
}
