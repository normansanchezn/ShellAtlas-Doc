import Foundation

public protocol AppServices: AnyObject {
    var getDocumentsUseCase: GetDocumentsUseCase { get }
    var getDocumentDetailUseCase: GetDocumentDetailUseCase { get }
    var evaluateDocumentHealthUseCase: EvaluateDocumentHealthUseCase { get }
    var generateUpdateProposalUseCase: GenerateUpdateProposalUseCase { get }
    var searchKnowledgeUseCase: SearchKnowledgeUseCase { get }
    var getRelatedSignalsUseCase: GetRelatedSignalsUseCase { get }
    var answerQuestionUseCase: AnswerQuestionUseCase { get }
    var askDocumentationAssistantUseCase: AskDocumentationAssistantUseCase { get }
    var checkDocumentationAssistantStatusUseCase: CheckDocumentationAssistantStatusUseCase { get }
    var getDocumentationTreeUseCase: GetDocumentationTreeUseCase { get }
    var getDocumentationDocumentsUseCase: GetDocumentationDocumentsUseCase { get }
    var getDocumentationDocumentDetailUseCase: GetDocumentationDocumentDetailUseCase { get }
    var searchDocumentsByTitleUseCase: SearchDocumentsByTitleUseCase { get }
    var createDocumentUseCase: CreateDocumentUseCase { get }
    var updateDocumentUseCase: UpdateDocumentUseCase { get }
    var deleteDocumentUseCase: DeleteDocumentUseCase { get }
    var saveDocumentationDraftUseCase: SaveDocumentationDraftUseCase { get }
    var getDocumentationVersionsUseCase: GetDocumentationVersionsUseCase { get }
    var restoreDocumentationVersionUseCase: RestoreDocumentationVersionUseCase { get }
    var documentRepository: any KnowledgeDocumentRepository { get }
    var ticketRepository: any TicketRepository { get }
    var signalRepository: any RepositorySignalRepository { get }
    var releaseRepository: any ReleaseRepository { get }
    var ownerRepository: any OwnerRepository { get }
}
