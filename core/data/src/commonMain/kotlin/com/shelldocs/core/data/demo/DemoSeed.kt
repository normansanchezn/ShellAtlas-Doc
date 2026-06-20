package com.shelldocs.core.data.demo

import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.data.repository.DerivedPendingUpdatesRepository
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.*
import com.shelldocs.core.domain.entity.source.*
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Deterministic demo dataset mirroring the original ShellEnterpriseDoc mock
 * sources, so every screen renders real content without a backend.
 */
object DemoSeed {

    private val parser = MarkdownParser()

    @OptIn(ExperimentalTime::class)
    val now: kotlin.time.Instant = kotlin.time.Instant.parse("2026-06-11T10:14:00Z")

    val elena = UserProfile("user-elena", "elena.vargas@shell.com", "Elena Vargas", "iOS Shell App", UserRole.OWNER)
    val marcus = UserProfile("user-marcus", "marcus.chen@shell.com", "Marcus Chen", "Loyalty Squad", UserRole.DEVELOP)
    val priya = UserProfile("user-priya", "priya.sharma@shell.com", "Priya Sharma", "iOS Shell App", UserRole.DEVELOP)
    val james = UserProfile("user-james", "james.obrien@shell.com", "James O'Brien", "Android Shell App", UserRole.BUSINESS)
    val sofia = UserProfile("user-sofia", "sofia.reyes@shell.com", "Sofia Reyes", "Platform Team", UserRole.DEVELOP)

    val teamMembers: List<TeamMember> = listOf(
        TeamMember(elena, isCurrentUser = true),
        TeamMember(marcus),
        TeamMember(priya),
        TeamMember(james),
        TeamMember(sofia),
    )

    @OptIn(ExperimentalTime::class)
    val documents: List<Document> = listOf(
        document(
            id = "doc-authentication",
            title = "Authentication",
            summary = "iOS login, token refresh, and session recovery behavior.",
            status = DocumentStatus.PUBLISHED,
            owner = elena,
            module = "Authentication",
            platform = "iOS",
            tags = listOf("auth", "tokens", "session"),
            updatedAt = kotlin.time.Instant.parse("2026-06-08T09:00:00Z"),
            markdown = """
                # Authentication

                The iOS authentication layer in Shell App follows a **silent token refresh** strategy using OAuth 2.0 with PKCE.

                ## Token Lifecycle

                Access tokens expire after **60 minutes**. When a request fails with `401 Unauthorized`, the client automatically attempts a silent refresh using the stored refresh token before prompting the user to re-authenticate.

                ## Refresh Logic

                1. The `AuthTokenManager` detects token expiration 5 minutes before the deadline (proactive refresh).
                2. A background request is sent to the token endpoint with the current refresh token.
                3. On success, both tokens are rotated and persisted in the iOS Keychain using `SecItemAdd` with `kSecAttrAccessibleAfterFirstUnlock`.
                4. On failure (e.g., network error or revoked session), the user is redirected to the login screen.

                ## Session Recovery

                Sessions are valid for **30 days** of inactivity. After expiry, full re-authentication via biometrics or PIN is required. The system preserves the user's last navigation state for seamless recovery.

                ## Edge Cases

                - Concurrent requests during refresh are queued until the new token is ready.
                - Background fetch tasks always carry a fresh token to avoid mid-sync failures.
                - Jailbroken device detection triggers immediate session invalidation.
            """.trimIndent(),
        ),
        document(
            id = "doc-loyalty",
            title = "Loyalty Rewards Flow",
            summary = "Points accrual, redemption, and tier progression logic.",
            status = DocumentStatus.OUTDATED,
            owner = marcus,
            module = "Loyalty",
            platform = "Cross-platform",
            team = "Loyalty Squad",
            tags = listOf("loyalty", "points", "rewards"),
            updatedAt = Instant.parse("2026-01-12T00:00:00Z"),
            area = Area.DEVELOPMENT,
            applicationVersion = "8.9.0",
            markdown = """
                # Loyalty Rewards Flow

                The loyalty rewards system manages Shell's customer points program across all mobile platforms.

                ## Points Accrual

                - Points are awarded at a rate of **1 point per litre** of fuel purchased.
                - Bonus multipliers apply during promotional periods.
                - Accrual events are queued offline and reconciled on the next sync.

                ## Redemption

                Points can be redeemed at Shell stations worldwide with a minimum threshold of **500 points**.

                ## Tier Progression

                1. Bronze: 0-999 points per quarter.
                2. Silver: 1,000-4,999 points per quarter.
                3. Gold: 5,000+ points per quarter with dedicated support.
            """.trimIndent(),
        ),
        document(
            id = "doc-station-locator",
            title = "Station Locator",
            summary = "Geolocation, proximity detection, and station data synchronization.",
            status = DocumentStatus.PUBLISHED,
            owner = priya,
            module = "Station Locator",
            platform = "iOS",
            tags = listOf("geolocation", "stations", "maps"),
            updatedAt = Instant.parse("2026-04-15T00:00:00Z"),
            markdown = """
                # Station Locator

                Station discovery combines on-device geolocation with a regional station cache.

                ## Proximity Detection

                - Significant-location changes wake the app to refresh nearby stations.
                - A geofence of 250 m triggers the "you are at a station" experience.

                ## Data Synchronization

                1. Regional station catalogs sync daily over delta endpoints.
                2. Offline mode serves the last cached catalog with a staleness banner.
                3. Conflicts resolve in favor of the server snapshot.
            """.trimIndent(),
        ),
        document(
            id = "doc-android-auth",
            title = "Android Authentication",
            summary = "Android-specific auth implementation using Credential Manager.",
            status = DocumentStatus.DRAFT,
            owner = james,
            module = "Authentication",
            platform = "Android",
            team = "Android Shell App",
            tags = listOf("auth", "android", "credential-manager"),
            updatedAt = Instant.parse("2026-05-20T00:00:00Z"),
            markdown = """
                # Android Authentication

                TODO: align with the iOS silent-refresh documentation once the Credential Manager rollout completes.

                ## Current State

                - Credential Manager handles passkey and password sign-in.
                - Tokens are stored in EncryptedSharedPreferences.
            """.trimIndent(),
        ),
        document(
            id = "doc-release-process",
            title = "Release Process",
            summary = "End-to-end process for staging, QA sign-off, and App Store submission.",
            status = DocumentStatus.PUBLISHED,
            owner = sofia,
            module = "Release Process",
            platform = "Process",
            team = "Platform Team",
            tags = listOf("release", "qa", "app-store", "eosb"),
            updatedAt = Instant.parse("2026-05-28T00:00:00Z"),
            markdown = """
                # Release Process

                The release train ships at the end of each sprint (EoSB) after a staged validation pipeline.

                ## Feature Complete

                - All acceptance criteria met, unit tests passing.

                ## Code Review

                - Minimum 2 senior engineers, security checklist signed.

                ## QA Sign-off

                - Full regression suite on physical devices.

                ## Staging Deploy

                - 48h soak period with monitoring.

                ## App Store Submission

                1. Binary upload and metadata review.
                2. Phased rollout from 10% to 100% over 72h.
            """.trimIndent(),
        ),
        document(
            id = "doc-eosb1",
            title = "EoSB1 Process for America's App - Android",
            summary = "Android EoSB1 release build, pilot branch and QA handoff process.",
            status = DocumentStatus.UPDATES_PENDING,
            owner = james,
            module = "Release Process",
            platform = "Android",
            team = "Android Shell App",
            tags = listOf("eosb1", "release", "build", "pilot", "qa", DerivedPendingUpdatesRepository.UPSTREAM_SIGNAL_TAGS.first()),
            updatedAt = Instant.parse("2026-02-18T00:00:00Z"),
            area = Area.DEVELOPMENT,
            applicationVersion = "9.6.0",
            markdown = """
                # EoSB1 Process for America's App - Android

                The Android EoSB1 process coordinates branch preparation, build generation and QA handoff for America's App.

                ## Branch Strategy

                - Prepare `develop`, `extra/pilot-8.99.0` and `madf/pilot`.
                - Validate pilot branch ownership before cutting a release candidate.

                ## Build Generation

                1. Update `build.gradle.kts` version values.
                2. Verify `updateconfig.py` and generated versionCodes.
                3. Run GitHub Actions release workflow for QA handoff.

                ## Validation

                - Confirm smoke tests on pilot branches.
                - Check Lokalise strings changes before sign-off.
                - Review Azure secrets required by the new build.

                TODO: document the newest rollback path after pilot rejection.
            """.trimIndent(),
        ),
        document(
            id = "doc-lokalise",
            title = "Lokalise Strings Update Process",
            summary = "Localization workflow for strings sync, review and release readiness.",
            status = DocumentStatus.PUBLISHED,
            owner = sofia,
            module = "Localization",
            platform = "Cross-platform",
            team = "Platform Team",
            tags = listOf("lokalise", "localization", "translations", "strings.xml"),
            updatedAt = Instant.parse("2026-06-05T00:00:00Z"),
            markdown = """
                # Lokalise Strings Update Process

                Localization updates are managed through Lokalise and validated before each release train.

                ## Export

                - Pull the latest strings from Lokalise.
                - Regenerate `strings.xml` and platform resource bundles.

                ## Review

                1. Check translation coverage for critical flows.
                2. Validate placeholder integrity.
                3. Confirm no outdated copy remains in pilot builds.

                ## Release Checks

                - Coordinate with QA for localization smoke tests.
                - Confirm release notes mention user-visible translation changes.
            """.trimIndent(),
        ),
        document(
            id = "doc-azure-secrets",
            title = "Azure Secrets Management for Mobile",
            summary = "Environment values, rotation steps and release-time secret checks.",
            status = DocumentStatus.PUBLISHED,
            owner = sofia,
            module = "Platform Security",
            platform = "Cross-platform",
            team = "Platform Team",
            tags = listOf("azure secrets", "keychain", "credentials", "environment values"),
            updatedAt = Instant.parse("2026-05-16T00:00:00Z"),
            markdown = """
                # Azure Secrets Management for Mobile

                Secret values for mobile release processes are stored in Azure-managed secret stores and mirrored into safe runtime environments.

                ## Rotation

                - Rotate values before major release trains.
                - Confirm dependent pipelines pick up the new environment values.

                ## Usage

                1. Validate secret names in release workflows.
                2. Confirm mobile clients still read the expected environment values.
                3. Re-check secure local storage such as Keychain or encrypted preferences where applicable.
            """.trimIndent(),
        ),
        document(
            id = "doc-onboarding-welcome",
            title = "Llega a Shell",
            summary = "Bienvenida al equipo: estructura, documentación y cómo usar el asistente de IA.",
            status = DocumentStatus.PUBLISHED,
            owner = sofia,
            module = "Onboarding",
            platform = "Onboarding",
            team = "Platform Team",
            tags = listOf("onboarding", "bienvenida", "knowledge-transfer"),
            updatedAt = Instant.parse("2026-06-10T00:00:00Z"),
            markdown = """
                # Llega a Shell

                ¡Bienvenido/a a Shell App! Esta guía es tu punto de partida para conocer el proyecto.

                ## Equipo

                - **iOS Shell App**: Elena Vargas (owner), Priya Sharma.
                - **Android Shell App**: James O'Brien.
                - **Loyalty Squad**: Marcus Chen.
                - **Platform Team**: Sofia Reyes.

                ## Dónde vive la documentación

                Toda la documentación del proyecto está organizada por carpetas en **Documentos**, agrupada por plataforma (iOS, Android, Cross-platform, Process, Onboarding). Cada documento tiene un owner, un módulo y un estado (Published, Draft, Outdated, Updates Pending).

                ## Cómo usar el asistente de IA

                El asistente responde preguntas sobre la documentación indexada y cita las fuentes que usó. También puede guiarte paso a paso por un recorrido de **Knowledge Transfer (KT)**: te indica un documento para revisar y, cuando le dices "listo", avanza al siguiente paso y actualiza tu progreso.

                ## Knowledge Transfer

                Para empezar tu recorrido guiado, abre el Asistente y pulsa **"Iniciar Knowledge Transfer"**. Tu progreso se refleja en el Dashboard como **Project Knowledge**.

                ## Siguiente paso

                Continúa con "Android Developer Setup" para preparar tu computadora.
            """.trimIndent(),
        ),
        document(
            id = "doc-onboarding-android-setup",
            title = "Android Developer Setup",
            summary = "Guía para preparar tu computadora: clonar el repo, variables de entorno y primer build (MOCK).",
            status = DocumentStatus.PUBLISHED,
            owner = james,
            module = "Onboarding",
            platform = "Onboarding",
            team = "Android Shell App",
            tags = listOf("onboarding", "android", "setup", "secrets"),
            updatedAt = Instant.parse("2026-06-10T00:00:00Z"),
            markdown = """
                # Android Developer Setup

                Guía para preparar tu entorno de desarrollo Android. **Todos los valores de esta guía son MOCK / TODO** y deben reemplazarse por los reales al incorporarte.

                ## Requisitos

                - Android Studio (última versión estable).
                - JDK 17.
                - Git configurado con tu cuenta corporativa.

                ## 1. Clonar el repositorio

                ```bash
                git clone git@github.com:shell/shell-app-android.git
                cd shell-app-android
                ```

                ## 2. Configurar `local.properties`

                Crea un archivo `local.properties` en la raíz del proyecto (no se versiona):

                ```properties
                # TODO: MOCK — reemplaza con tu SDK real
                sdk.dir=/Users/TU_USUARIO/Library/Android/sdk

                # TODO: MOCK — obtener de Azure Secrets, ver "Azure Secrets Management for Mobile"
                API_BASE_URL=https://api.mock.shell.internal
                API_CLIENT_ID=mock-client-id
                API_CLIENT_SECRET=mock-client-secret
                LOKALISE_API_TOKEN=mock-lokalise-token
                ```

                ## 3. Variables de entorno

                ```bash
                # TODO: MOCK — valores reales en Azure Key Vault
                export SHELL_ANDROID_SIGNING_KEY_ALIAS="mock-alias"
                export SHELL_ANDROID_SIGNING_KEY_PASSWORD="mock-password"
                ```

                ## 4. Primer build

                ```bash
                ./gradlew :composeApp:assembleDebug
                ```

                ## 5. Siguiente paso

                Cuando termines, vuelve al asistente y dile "listo" para continuar el Knowledge Transfer.
            """.trimIndent(),
        ),
        document(
            id = "doc-push-notifications",
            title = "Push Notifications",
            summary = "APNS integration, token management, and notification categories.",
            status = DocumentStatus.UPDATES_PENDING,
            owner = elena,
            module = "Authentication",
            platform = "iOS",
            tags = listOf("push", "apns", "notifications"),
            updatedAt = Instant.parse("2026-04-05T00:00:00Z"),
            markdown = """
                # Push Notifications

                APNS powers transactional and marketing notifications.

                ## Token Management

                - Device tokens re-register on every app launch.
                - Tokens rotate when the user signs out or restores a backup.

                ## Categories

                - `OFFERS`: marketing pushes, user-toggleable.
                - `ACCOUNT`: security alerts, always on.
                - `STATION`: proximity offers tied to geofences.
            """.trimIndent(),
        ),
    )

    @OptIn(ExperimentalTime::class)
    val sources: List<KnowledgeSource> = listOf(
        KnowledgeSource(
            id = "source-confluence",
            kind = SourceKind.CONFLUENCE,
            host = "shell-engineering.atlassian.net",
            status = SourceStatus.CONNECTED,
            importedDocs = 84,
            lastSyncAt = kotlin.time.Instant.parse("2026-06-11T08:14:00Z"),
        ),
        KnowledgeSource(
            id = "source-azure",
            kind = SourceKind.AZURE_DEVOPS,
            host = "dev.azure.com/shell-eng",
            status = SourceStatus.CONNECTED,
            importedDocs = 41,
            lastSyncAt = Instant.parse("2026-06-11T06:02:00Z"),
        ),
        KnowledgeSource(
            id = "source-jira",
            kind = SourceKind.JIRA,
            host = "shell-jira.atlassian.net",
            status = SourceStatus.ERROR,
            importedDocs = 22,
            lastSyncAt = Instant.parse("2026-06-09T23:30:00Z"),
            errorMessage = "Authentication token expired. Please reconnect.",
        ),
    )

    @OptIn(ExperimentalTime::class)
    val syncLog: List<SyncLogEntry> = listOf(
        SyncLogEntry("log-1", SourceKind.CONFLUENCE, SyncOutcome.SUCCESS, "Confluence sync completed", 3, 7, Instant.parse("2026-06-11T10:14:00Z")),
        SyncLogEntry("log-2", SourceKind.AZURE_DEVOPS, SyncOutcome.SUCCESS, "Azure DevOps sync completed", 0, 2, Instant.parse("2026-06-11T08:02:00Z")),
        SyncLogEntry("log-3", SourceKind.JIRA, SyncOutcome.FAILURE, "Jira sync failed — token expired", 0, 0, Instant.parse("2026-06-09T23:30:00Z")),
        SyncLogEntry("log-4", SourceKind.CONFLUENCE, SyncOutcome.SUCCESS, "Confluence sync completed", 1, 4, Instant.parse("2026-06-09T10:00:00Z")),
        SyncLogEntry("log-5", SourceKind.AZURE_DEVOPS, SyncOutcome.SUCCESS, "Azure DevOps sync completed", 2, 0, Instant.parse("2026-06-09T08:00:00Z")),
    )

    @OptIn(ExperimentalTime::class)
    private fun document(
        id: String,
        title: String,
        summary: String,
        status: DocumentStatus,
        owner: UserProfile,
        module: String,
        platform: String,
        markdown: String,
        tags: List<String>,
        updatedAt: kotlin.time.Instant,
        team: String = owner.team,
        area: Area? = null,
        applicationVersion: String = "",
    ): Document {
        val parsed = parser.parse(markdown)
        return Document(
            id = id,
            title = title,
            summary = summary,
            status = status,
            classification = DocumentClassification.INTERNAL,
            rawMarkdown = markdown,
            content = parsed.content,
            plainText = parsed.plainText,
            attributes = DocumentAttributes(
                owner = owner.fullName,
                module = module,
                team = team,
                platform = platform,
                tags = tags,
                area = area,
                applicationVersion = applicationVersion,
            ),
            createdAt = Instant.parse("2025-11-03T00:00:00Z"),
            updatedAt = updatedAt,
        )
    }
}
