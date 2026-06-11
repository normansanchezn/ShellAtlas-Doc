import Foundation
import SD_Data
import SD_Domain

@Observable
final class AppContainer: AppServices {
    let environment: AppEnvironment

    let documentRepository: any KnowledgeDocumentRepository
    let ticketRepository: any TicketRepository
    let signalRepository: any RepositorySignalRepository
    let releaseRepository: any ReleaseRepository
    let ownerRepository: any OwnerRepository

    let getDocumentsUseCase: GetDocumentsUseCase
    let getDocumentDetailUseCase: GetDocumentDetailUseCase
    let searchKnowledgeUseCase: SearchKnowledgeUseCase
    let evaluateDocumentHealthUseCase: EvaluateDocumentHealthUseCase
    let getRelatedSignalsUseCase: GetRelatedSignalsUseCase
    let generateUpdateProposalUseCase: GenerateUpdateProposalUseCase
    let answerQuestionUseCase: AnswerQuestionUseCase
    let askDocumentationAssistantUseCase: AskDocumentationAssistantUseCase
    let checkDocumentationAssistantStatusUseCase: CheckDocumentationAssistantStatusUseCase
    let getDocumentationTreeUseCase: GetDocumentationTreeUseCase
    let getDocumentationDocumentsUseCase: GetDocumentationDocumentsUseCase
    let getDocumentationDocumentDetailUseCase: GetDocumentationDocumentDetailUseCase
    let searchDocumentsByTitleUseCase: SearchDocumentsByTitleUseCase
    let createDocumentUseCase: CreateDocumentUseCase
    let updateDocumentUseCase: UpdateDocumentUseCase
    let deleteDocumentUseCase: DeleteDocumentUseCase
    let saveDocumentationDraftUseCase: SaveDocumentationDraftUseCase
    let getDocumentationVersionsUseCase: GetDocumentationVersionsUseCase
    let restoreDocumentationVersionUseCase: RestoreDocumentationVersionUseCase

    init(environment: AppEnvironment = .current) {
        self.environment = environment

        ShellDocConnectionLogger.log("runtime_environment_loaded", fields: [
            "api_base_url_raw_present": ProcessInfo.processInfo.environment["SHELLDOC_API_BASE_URL"]?.isEmpty == false,
            "api_base_url_parsed": environment.shellDocAPIBaseURL?.absoluteString,
            "api_base_url_valid": environment.shellDocAPIBaseURL != nil,
            "ollama_base_url_raw_present": ProcessInfo.processInfo.environment["SHELLDOC_OLLAMA_BASE_URL"]?.isEmpty == false,
            "ollama_base_url_parsed": environment.ollamaBaseURL.absoluteString,
            "ollama_model": environment.ollamaModel,
            "ollama_enabled": environment.usesOllamaAssistant,
            "api_bearer_token_present": environment.shellDocAPIBearerToken?.isEmpty == false
        ])

        let docRepo = MockKnowledgeDocumentRepository()
        let documentationRepo: any DocumentationRepository
        let documentationTreeRepo: any DocumentationTreeRepository
        let intelligenceCache: (any AssistantIntelligenceCacheRepository)?
        if let apiBaseURL = environment.shellDocAPIBaseURL {
            ShellDocConnectionLogger.log("backend_api_configured", fields: [
                "storage_source": "supabase_postgres_via_backend_api",
                "backend": "ShellDoc Backend API",
                "api_base_url": apiBaseURL.absoluteString,
                "auth": environment.shellDocAPIBearerToken?.isEmpty == false ? "bearer_token_configured_redacted" : "none",
                "supabase_connection": "server_side_only",
                "client_has_supabase_service_role": false,
                "client_has_atlassian_secret": false,
                "client_has_github_private_key": false
            ])
            let apiClient = ShellDocAPIClient(
                configuration: ShellDocAPIConfiguration(
                    baseURL: apiBaseURL,
                    bearerToken: environment.shellDocAPIBearerToken
                )
            )
            let remoteRepo = RemoteDocumentationRepository(
                remoteDataSource: APIDocumentRemoteDataSource(client: apiClient)
            )
            documentationRepo = CachingDocumentationRepository(wrapped: remoteRepo)
            documentationTreeRepo = DocumentationRepositoryTreeRepository(documentationRepository: documentationRepo)
            intelligenceCache = RemoteAssistantIntelligenceCacheRepository(client: apiClient)
        } else {
            ShellDocConnectionLogger.log("mock_storage_fallback", fields: [
                "storage_source": "local_mock_preview_or_development",
                "reason": "SHELLDOC_API_BASE_URL is not configured",
                "supabase_connection": "not_configured",
                "backend": "none"
            ])
            documentationRepo = MockDocumentationRepository()
            documentationTreeRepo = MockDocumentationTreeRepository()
            intelligenceCache = nil
        }
        let ticketRepo = MockTicketRepository()
        let signalRepo = MockRepositorySignalRepository()
        let releaseRepo = MockReleaseRepository()
        let ownerRepo = MockOwnerRepository()

        self.documentRepository = docRepo
        self.ticketRepository = ticketRepo
        self.signalRepository = signalRepo
        self.releaseRepository = releaseRepo
        self.ownerRepository = ownerRepo

        let search = SearchKnowledgeUseCase(repository: docRepo)

        self.getDocumentsUseCase = GetDocumentsUseCase(repository: docRepo)
        self.getDocumentDetailUseCase = GetDocumentDetailUseCase(repository: docRepo)
        self.searchKnowledgeUseCase = search
        self.evaluateDocumentHealthUseCase = EvaluateDocumentHealthUseCase(
            ticketRepository: ticketRepo,
            signalRepository: signalRepo,
            releaseRepository: releaseRepo
        )
        self.getRelatedSignalsUseCase = GetRelatedSignalsUseCase(
            ticketRepository: ticketRepo,
            signalRepository: signalRepo,
            releaseRepository: releaseRepo
        )
        self.generateUpdateProposalUseCase = GenerateUpdateProposalUseCase(
            ticketRepository: ticketRepo,
            signalRepository: signalRepo,
            releaseRepository: releaseRepo
        )
        self.answerQuestionUseCase = AnswerQuestionUseCase(
            documentRepository: docRepo,
            ticketRepository: ticketRepo,
            signalRepository: signalRepo,
            releaseRepository: releaseRepo,
            searchUseCase: search
        )

        let retrievalRepository = LocalDocumentationRetrievalRepository(documentationRepository: documentationRepo)
        let retrievalUseCase = RetrieveDocumentationSnippetsUseCase(repository: retrievalRepository)
        self.askDocumentationAssistantUseCase = AskDocumentationAssistantUseCase(
            retrievalUseCase: retrievalUseCase,
            promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
            localLLMClient: RuntimeDocumentationAssistantLLMClient(environment: environment),
            intelligenceCache: intelligenceCache
        )
        self.checkDocumentationAssistantStatusUseCase = CheckDocumentationAssistantStatusUseCase(
            repository: OllamaLocalLLMStatusRepository(
                baseURL: environment.ollamaBaseURL,
                model: environment.ollamaModel
            )
        )

        self.getDocumentationTreeUseCase = GetDocumentationTreeUseCase(repository: documentationTreeRepo)
        self.getDocumentationDocumentsUseCase = GetDocumentationDocumentsUseCase(repository: documentationRepo)
        self.getDocumentationDocumentDetailUseCase = GetDocumentationDocumentDetailUseCase(repository: documentationRepo)
        self.searchDocumentsByTitleUseCase = SearchDocumentsByTitleUseCase(repository: documentationRepo)
        self.createDocumentUseCase = CreateDocumentUseCase(repository: documentationRepo)
        self.updateDocumentUseCase = UpdateDocumentUseCase(repository: documentationRepo)
        self.deleteDocumentUseCase = DeleteDocumentUseCase(repository: documentationRepo)
        self.saveDocumentationDraftUseCase = SaveDocumentationDraftUseCase(repository: documentationRepo)
        self.getDocumentationVersionsUseCase = GetDocumentationVersionsUseCase(repository: documentationRepo)
        self.restoreDocumentationVersionUseCase = RestoreDocumentationVersionUseCase(repository: documentationRepo)
    }
}

private final class RuntimeDocumentationAssistantLLMClient: LocalLLMClient, @unchecked Sendable {
    private let environment: AppEnvironment

    init(environment: AppEnvironment) {
        self.environment = environment
    }

    func generateAnswer(prompt: String) async throws -> String {
        let userEnabledOllama = UserDefaults.standard.bool(
            forKey: DocumentationAssistantConfiguration.usesOllamaKey
        )
        let shouldUseOllama = environment.usesOllamaAssistant || userEnabledOllama

        ShellDocConnectionLogger.log("assistant_llm_selected", fields: [
            "provider": shouldUseOllama ? "ollama" : "mock",
            "ollama_base_url": environment.ollamaBaseURL.absoluteString,
            "ollama_model": environment.ollamaModel,
            "enabled_by_environment": environment.usesOllamaAssistant,
            "enabled_by_user_defaults": userEnabledOllama
        ])

        if shouldUseOllama {
            return try await OllamaLocalLLMClient(
                baseURL: environment.ollamaBaseURL,
                model: environment.ollamaModel
            )
            .generateAnswer(prompt: prompt)
        }

        return try await MockLocalLLMClient().generateAnswer(prompt: prompt)
    }
}
