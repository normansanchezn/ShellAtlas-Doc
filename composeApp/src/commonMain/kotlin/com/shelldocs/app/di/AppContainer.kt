package com.shelldocs.app.di

import com.shelldocs.app.AppAuthRepository
import com.shelldocs.app.NoOpSessionPreferences
import com.shelldocs.app.SessionPreferences
import com.shelldocs.app.navigation.AppNavigator
import com.shelldocs.core.common.coroutines.DefaultDispatcherProvider
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.id.RandomIdGenerator
import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
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
import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.repository.AuthRepository
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.RoleRepository
import com.shelldocs.core.domain.usecase.assistant.*
import com.shelldocs.core.domain.usecase.auth.*
import com.shelldocs.core.domain.usecase.classification.ApplyMetadataAssignmentsUseCase
import com.shelldocs.core.domain.usecase.classification.AssignMetadataUseCase
import com.shelldocs.core.domain.usecase.classification.GetMetadataIssuesUseCase
import com.shelldocs.core.domain.usecase.dashboard.GetDashboardMetricsUseCase
import com.shelldocs.core.domain.usecase.document.*
import com.shelldocs.core.domain.usecase.onboarding.CompleteKnowledgeCheckpointUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeCheckpointsUseCase
import com.shelldocs.core.domain.usecase.onboarding.GetKnowledgeProgressUseCase
import com.shelldocs.core.domain.usecase.updates.GetHealthyDocumentsUseCase
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
import io.ktor.client.plugins.*
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
            // Local LLM generation on a big prompt can take minutes on CPU-only
            // Ollama; the per-engine defaults (10s on OkHttp/Darwin) cut that off
            // far too early, so override them platform-wide.
            install(HttpTimeout) {
                requestTimeoutMillis = 5 * 60 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 5 * 60 * 1000
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

    fun currentLanguage(): com.shelldocs.core.domain.entity.auth.AppLanguage =
        authRepository.session.value?.user?.language ?: com.shelldocs.core.domain.entity.auth.AppLanguage.ENGLISH

    private fun currentArea() =
        Area.fromKey(authRepository.session.value?.user?.team)

    // --- Documents --------------------------------------------------------

    // Read path: API direct when configured (it's the live Confluence-backed
    // backend), Supabase only as fallback. Supabase is also kept as a mirror
    // by [syncDocumentsFromApi] in the background, independent of reads.
    private val supabaseDocumentRepository: SupabaseDocumentRepository? by lazy {
        config.supabase?.let { supabase ->
            SupabaseDocumentRepository(
                postgrest = postgrest(supabase),
                timeProvider = timeProvider,
                currentUserIdProvider = { authRepository.session.value?.user?.id },
            )
        }
    }

    private val documentRepository: DocumentRepository by lazy {
        val api = config.api
        val supabaseRepo = supabaseDocumentRepository
        when {
            api != null -> CachingDocumentRepository(ApiDocumentRepository(ShellDocsApi(httpClient, api)))
            supabaseRepo != null -> CachingDocumentRepository(supabaseRepo)
            else -> DemoDocumentRepository(timeProvider)
        }
    }
    private val cachingDocumentRepository: CachingDocumentRepository?
        get() = documentRepository as? CachingDocumentRepository
    private val treeRepository by lazy { DerivedDocumentTreeRepository(documentRepository) }

    // --- Assistant --------------------------------------------------------

    private val ollamaClient: OllamaClient? by lazy {
        if (config.useOllama) OllamaClient(httpClient, config.ollama) else null
    }

    private val evaluateHealth by lazy { EvaluateDocumentHealthUseCase(timeProvider) }
    private val assistantEngine by lazy {
        val grounded = GroundedAssistantEngine(ShouldImproveDocumentUseCase(evaluateHealth))
        val ollama = ollamaClient
        if (ollama != null) {
            CompositeAssistantEngine(
                primary = OllamaAssistantEngine(ollama),
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
    private val connectionsRepository by lazy {
        RealConnectionsRepository(
            ollamaClient = ollamaClient,
            api = config.api?.let { ShellDocsApi(httpClient, it) },
            postgrest = config.supabase?.let { postgrest(it) },
        )
    }

    // --- Startup diagnostics ------------------------------------------------

    /**
     * Runs once per app launch: confirms DB, Ollama and Jira/Confluence/Azure
     * connectivity and logs the outcome of each under [LogTags.STARTUP] (plus
     * their dedicated tags) so issues are visible in Logcat/console right away
     * instead of surfacing later as an opaque UI error.
     */
    suspend fun runStartupDiagnostics() {
        val startupLogger = AppLogger.tag(LogTags.STARTUP)
        startupLogger.i("Running startup diagnostics (env=${config.environment}, demoMode=${config.isDemoMode})")
        checkDatabaseConnection()
        checkOllamaConnection()
        checkIntegrations()
    }

    /**
     * Mirrors the API document list into Supabase, writing only what
     * actually changed (content hash mismatch or missing row). Must run
     * *after* sign-in: row writes are RLS-gated on `auth.uid()` + owner/develop
     * role, so calling this before a session exists fails every write. Does
     * not gate what the Documents screen shows (that reads the API directly);
     * this is a best-effort background mirror only.
     */
    suspend fun syncDocumentsFromApi() {
        val syncLogger = AppLogger.tag(LogTags.INTEGRATION)
        val api = config.api
        val supabaseRepo = supabaseDocumentRepository
        if (api == null || supabaseRepo == null) {
            syncLogger.i("Skipped documents sync: requires both api and supabase config")
            return
        }
        if (authRepository.session.value?.accessToken == null) {
            syncLogger.i("Skipped documents sync: no authenticated session yet")
            return
        }
        val client = ShellDocsApi(httpClient, api)
        val externalDocuments = try {
            client.documents()
        } catch (error: Exception) {
            syncLogger.e("Document sync failed to fetch from API: ${error.message}", error)
            return
        }
        val result = supabaseRepo.syncFromExternal(
            sourceType = "shelldocs_api",
            externalDocuments = externalDocuments.map {
                ExternalDocumentInput(externalId = it.id, title = it.title, rawMarkdown = it.rawMarkdown)
            },
        )
        result
            .onSuccess { summary ->
                syncLogger.i(
                    "Document sync complete: created=${summary.created} " +
                            "updated=${summary.updated} skipped=${summary.skipped}",
                )
                if (summary.created > 0 || summary.updated > 0) cachingDocumentRepository?.invalidate()
            }
            .onFailure { error -> syncLogger.e("Document sync failed: $error") }
    }

    private suspend fun checkDatabaseConnection() {
        val dbLogger = AppLogger.tag(LogTags.DATABASE)
        val supabase = config.supabase
        if (supabase == null) {
            dbLogger.i("Skipped: no Supabase config (demo mode, in-memory data)")
            return
        }
        postgrest(supabase).testConnection()
    }

    private suspend fun checkOllamaConnection() {
        val ollamaLogger = AppLogger.tag(LogTags.OLLAMA)
        val client = ollamaClient
        if (client == null) {
            ollamaLogger.i("Skipped: Ollama disabled (useOllama=false)")
            return
        }
        client.isReachable()
    }

    /**
     * Count for the sidebar/rail badge: union of documents needing attention, by id.
     * A document flagged unhealthy *and* with metadata issues counts once, not twice.
     */
    suspend fun pendingUpdatesCount(): Int {
        val unhealthyIds = GetPendingUpdatesUseCase(pendingUpdatesRepository)()
            .getOrDefault(emptyList())
            .map { it.documentId }
        val metadataIssueIds = GetMetadataIssuesUseCase(documentClassificationRepository)()
            .getOrDefault(emptyList())
            .map { it.documentId }
        return (unhealthyIds + metadataIssueIds).toSet().size
    }

    private suspend fun checkIntegrations() {
        val integrationLogger = AppLogger.tag(LogTags.INTEGRATION)
        integrationLogger.i("Checking Jira/Confluence/Azure DevOps integrations")
        sourcesRepository.sources()
    }

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
        sessionPrefs = sessionPrefs,
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
        deleteDocument = DeleteDocumentUseCase(documentRepository),
        roleProvider = ::currentRole,
        dispatchers = dispatchers,
        openDocumentRequests = navigator.openDocumentRequests,
        consumeOpenDocumentRequest = navigator::consumeOpenDocumentRequest,
    )

    fun updatesViewModel() = UpdatesViewModel(
        getPendingUpdates = GetPendingUpdatesUseCase(pendingUpdatesRepository),
        scanForUpdates = ScanForUpdatesUseCase(pendingUpdatesRepository),
        getMetadataIssues = GetMetadataIssuesUseCase(documentClassificationRepository),
        getHealthyDocuments = GetHealthyDocumentsUseCase(pendingUpdatesRepository),
        applyMetadataAssignments = ApplyMetadataAssignmentsUseCase(documentClassificationRepository),
        assignMetadata = AssignMetadataUseCase(documentClassificationRepository),
        setManualRiskLevel = SetManualRiskLevelUseCase(pendingUpdatesRepository),
        currentUserRole = currentRole(),
        visibleArea = currentArea(),
        canUpdateDocuments = RolePermissions.isGranted(currentRole(), Permission.PUBLISH_DOCUMENTS),
        dispatchers = dispatchers,
    )

    fun aiUpdateViewModel() = AiUpdateViewModel(
        generateSuggestedUpdate = GenerateSuggestedUpdateUseCase(documentRepository, evaluateHealth, timeProvider),
        saveDraft = SaveDraftUseCase(documentRepository),
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
        connectionsRepository = connectionsRepository,
        dispatchers = dispatchers,
    )

    fun settingsViewModel() = SettingsViewModel(
        getTeamMembers = GetTeamMembersUseCase(roleRepository),
        assignRole = AssignRoleUseCase(roleRepository),
        signOut = SignOutUseCase(authRepository),
        updateLanguage = UpdateLanguageUseCase(authRepository),
        roleProvider = ::currentRole,
        languageProvider = ::currentLanguage,
        dispatchers = dispatchers,
    )
}
