package com.shelldocs.app.di

import com.shelldocs.app.AppAuthRepository
import com.shelldocs.app.NoOpSessionPreferences
import com.shelldocs.app.SessionPreferences
import com.shelldocs.app.navigation.AppNavigator
import com.shelldocs.core.common.coroutines.DefaultDispatcherProvider
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.id.RandomIdGenerator
import com.shelldocs.core.common.time.SystemTimeProvider
import com.shelldocs.core.data.assistant.CompositeAssistantEngine
import com.shelldocs.core.data.assistant.GroundedAssistantEngine
import com.shelldocs.core.data.assistant.OllamaAssistantEngine
import com.shelldocs.core.data.assistant.OllamaClient
import com.shelldocs.core.data.demo.*
import com.shelldocs.core.data.network.ShellDocsApi
import com.shelldocs.core.data.repository.*
import com.shelldocs.core.data.supabase.SupabaseAuthApi
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import com.shelldocs.core.data.supabase.SupabaseProfileDataSource
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.DevelopmentArea
import com.shelldocs.core.domain.repository.AuthRepository
import com.shelldocs.core.domain.repository.RoleRepository
import com.shelldocs.core.domain.usecase.assistant.*
import com.shelldocs.core.domain.usecase.auth.AssignRoleUseCase
import com.shelldocs.core.domain.usecase.auth.GetTeamMembersUseCase
import com.shelldocs.core.domain.usecase.auth.SignInUseCase
import com.shelldocs.core.domain.usecase.auth.SignOutUseCase
import com.shelldocs.core.domain.usecase.classification.AcceptMetadataSuggestionUseCase
import com.shelldocs.core.domain.usecase.classification.AssignMetadataUseCase
import com.shelldocs.core.domain.usecase.classification.GetMetadataIssuesUseCase
import com.shelldocs.core.domain.usecase.dashboard.GetDashboardMetricsUseCase
import com.shelldocs.core.domain.usecase.document.*
import com.shelldocs.core.domain.usecase.onboarding.CompleteKnowledgeCheckpointUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeCheckpointsUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeProgressUseCase
import com.shelldocs.core.domain.usecase.source.GetSourcesUseCase
import com.shelldocs.core.domain.usecase.source.GetSyncLogUseCase
import com.shelldocs.core.domain.usecase.source.SyncSourceUseCase
import com.shelldocs.core.domain.usecase.updates.GetPendingUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.SetManualRiskLevelUseCase
import com.shelldocs.feature.assistant.presentation.AssistantViewModel
import com.shelldocs.feature.auth.presentation.AuthViewModel
import com.shelldocs.feature.dashboard.presentation.DashboardViewModel
import com.shelldocs.feature.documents.presentation.DocumentsViewModel
import com.shelldocs.feature.settings.presentation.SettingsViewModel
import com.shelldocs.feature.sources.presentation.SourcesViewModel
import com.shelldocs.feature.updates.presentation.AiUpdateViewModel
import com.shelldocs.feature.updates.presentation.UpdatesViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Manual composition root (no DI framework): one place where the whole
 * dependency graph is assembled, mirroring the original AppContainer.
 */
class AppContainer(
    private val config: AppConfig = AppConfig(),
    private val sessionPrefs: SessionPreferences = NoOpSessionPreferences,
) {

    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    private val timeProvider = SystemTimeProvider()
    private val idGenerator = RandomIdGenerator()

    val navigator = AppNavigator()

    init {
        if (config.environment == AppEnvironment.PROD && config.api == null && config.supabase == null) {
            error("ShellDoc PROD requires API or Supabase configuration")
        }
    }

    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }

    // --- Auth & roles -----------------------------------------------------

    val authRepository: AuthRepository by lazy {
        val supabase = config.supabase
        val delegate: AuthRepository = if (supabase == null) {
            DemoAuthRepository(timeProvider, initiallyLoggedIn = sessionPrefs.loadSessionFlag())
        } else {
            SupabaseAuthRepository(
                authApi = SupabaseAuthApi(httpClient, supabase),
                profiles = SupabaseProfileDataSource(postgrest(supabase)),
                roleRepository = roleRepository as SupabaseRoleRepository,
                timeProvider = timeProvider,
            )
        }
        AppAuthRepository(delegate, sessionPrefs)
    }

    val roleRepository: RoleRepository by lazy {
        val supabase = config.supabase
        if (supabase == null) {
            DemoRoleRepository()
        } else {
            SupabaseRoleRepository(
                postgrest = postgrest(supabase),
                profiles = SupabaseProfileDataSource(postgrest(supabase)),
                currentUserIdProvider = { authRepository.session.value?.user?.id },
            )
        }
    }

    private fun postgrest(supabase: com.shelldocs.core.data.supabase.SupabaseConfig) =
        SupabasePostgrestApi(httpClient, supabase) { authRepository.session.value?.accessToken }

    private fun currentRole(): UserRole =
        authRepository.session.value?.user?.role ?: UserRole.VIEWER

    private fun currentDevelopmentArea() =
        DevelopmentArea.fromKey(authRepository.session.value?.user?.team)

    // --- Documents --------------------------------------------------------

    private val documentRepository by lazy {
        val api = config.api
        val supabase = config.supabase
        if (api != null) {
            CachingDocumentRepository(ApiDocumentRepository(ShellDocsApi(httpClient, api)))
        } else if (supabase != null) {
            CachingDocumentRepository(
                SupabaseDocumentRepository(
                    postgrest = postgrest(supabase),
                    timeProvider = timeProvider,
                    currentUserIdProvider = { authRepository.session.value?.user?.id },
                ),
            )
        } else {
            DemoDocumentRepository(timeProvider)
        }
    }
    private val treeRepository by lazy { DerivedDocumentTreeRepository(documentRepository) }

    // --- Assistant --------------------------------------------------------

    private val evaluateHealth by lazy { EvaluateDocumentHealthUseCase(timeProvider) }
    private val assistantEngine by lazy {
        val grounded = GroundedAssistantEngine(ShouldImproveDocumentUseCase(evaluateHealth))
        if (config.useOllama) {
            CompositeAssistantEngine(
                primary = OllamaAssistantEngine(OllamaClient(httpClient, config.ollama)),
                fallback = grounded,
            )
        } else {
            grounded
        }
    }
    private val assistantCache by lazy { InMemoryAssistantCacheRepository() }
    private val conversationRepository by lazy { InMemoryConversationRepository() }

    // --- Operations -------------------------------------------------------

    private val pendingUpdatesRepository by lazy {
        DerivedPendingUpdatesRepository(documentRepository, evaluateHealth, timeProvider)
    }

    private val documentClassificationRepository by lazy {
        DerivedDocumentClassificationRepository(documentRepository, timeProvider)
    }
    private val knowledgeCheckpointRepository by lazy { DemoKnowledgeCheckpointRepository() }
    private val dashboardRepository by lazy {
        DerivedDashboardRepository(documentRepository, evaluateHealth, knowledgeCheckpointRepository)
    }
    private val sourcesRepository by lazy { DemoSourcesRepository(timeProvider) }
    private val documentSyncRepository by lazy { DemoDocumentSyncRepository() }

    // --- ViewModel factories (one fresh instance per screen entry) --------

    fun authViewModel() = AuthViewModel(
        signIn = SignInUseCase(authRepository),
        dispatchers = dispatchers,
    )

    fun assistantViewModel() = AssistantViewModel(
        askAssistant = AskAssistantUseCase(
            detectIntent = DetectAssistantIntentUseCase(),
            retrieveGroundingDocuments = RetrieveGroundingDocumentsUseCase(documentRepository),
            engine = assistantEngine,
            cache = assistantCache,
            createDocumentFromAssistant = CreateDocumentFromAssistantUseCase(CreateDocumentUseCase(documentRepository)),
            roleProvider = ::currentRole,
        ),
        checkAvailability = CheckAssistantAvailabilityUseCase(assistantEngine),
        getConversations = GetConversationsUseCase(conversationRepository),
        saveConversation = SaveConversationUseCase(conversationRepository),
        getDocuments = GetDocumentsUseCase(documentRepository),
        getKnowledgeCheckpoints = GetKnowledgeCheckpointsUseCase(knowledgeCheckpointRepository),
        getKnowledgeProgress = GetKnowledgeProgressUseCase(knowledgeCheckpointRepository),
        completeKnowledgeCheckpoint = CompleteKnowledgeCheckpointUseCase(knowledgeCheckpointRepository),
        timeProvider = timeProvider,
        idGenerator = idGenerator,
        dispatchers = dispatchers,
    )

    fun documentsViewModel() = DocumentsViewModel(
        getDocuments = GetDocumentsUseCase(documentRepository),
        getDocumentTree = GetDocumentTreeUseCase(treeRepository),
        saveDraft = SaveDraftUseCase(documentRepository),
        publishDocument = PublishDocumentUseCase(documentRepository),
        getVersions = GetDocumentVersionsUseCase(documentRepository),
        restoreVersion = RestoreDocumentVersionUseCase(documentRepository),
        createDocument = CreateDocumentUseCase(documentRepository),
        updateAttributes = UpdateDocumentAttributesUseCase(documentRepository),
        roleProvider = ::currentRole,
        dispatchers = dispatchers,
        openDocumentRequests = navigator.openDocumentRequests,
        consumeOpenDocumentRequest = navigator::consumeOpenDocumentRequest,
    )

    fun updatesViewModel() = UpdatesViewModel(
        getPendingUpdates = GetPendingUpdatesUseCase(pendingUpdatesRepository),
        scanForUpdates = ScanForUpdatesUseCase(pendingUpdatesRepository),
        getMetadataIssues = GetMetadataIssuesUseCase(documentClassificationRepository),
        acceptMetadataSuggestion = AcceptMetadataSuggestionUseCase(documentClassificationRepository),
        assignMetadata = AssignMetadataUseCase(documentClassificationRepository),
        setManualRiskLevel = SetManualRiskLevelUseCase(pendingUpdatesRepository),
        currentUserRole = currentRole(),
        visibleDevelopmentArea = currentDevelopmentArea(),
        canUpdateDocuments = RolePermissions.isGranted(currentRole(), Permission.PUBLISH_DOCUMENTS),
        dispatchers = dispatchers,
    )

    fun aiUpdateViewModel() = AiUpdateViewModel(
        generateSuggestedUpdate = GenerateSuggestedUpdateUseCase(documentRepository, evaluateHealth, timeProvider),
        publishDocument = PublishDocumentUseCase(documentRepository),
        updateAttributes = UpdateDocumentAttributesUseCase(documentRepository),
        syncToSourcesOfTruth = SyncDocumentToSourcesOfTruthUseCase(documentSyncRepository),
        roleProvider = ::currentRole,
        currentUserProvider = { authRepository.session.value?.user },
        dispatchers = dispatchers,
        documentIdRequests = navigator.aiUpdateRequests,
        consumeDocumentIdRequest = navigator::consumeAiUpdateRequest,
    )

    fun dashboardViewModel() = DashboardViewModel(
        getDashboardMetrics = GetDashboardMetricsUseCase(dashboardRepository),
        dispatchers = dispatchers,
    )

    fun sourcesViewModel() = SourcesViewModel(
        getSources = GetSourcesUseCase(sourcesRepository),
        getSyncLog = GetSyncLogUseCase(sourcesRepository),
        syncSource = SyncSourceUseCase(sourcesRepository),
        sourcesRepository = sourcesRepository,
        roleProvider = ::currentRole,
        dispatchers = dispatchers,
    )

    fun settingsViewModel() = SettingsViewModel(
        getTeamMembers = GetTeamMembersUseCase(roleRepository),
        assignRole = AssignRoleUseCase(roleRepository),
        signOut = SignOutUseCase(authRepository),
        roleProvider = ::currentRole,
        dispatchers = dispatchers,
    )
}
