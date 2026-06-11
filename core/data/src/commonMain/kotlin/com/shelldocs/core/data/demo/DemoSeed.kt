package com.shelldocs.core.data.demo

import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SourceKind
import com.shelldocs.core.domain.entity.source.SourceStatus
import com.shelldocs.core.domain.entity.source.SyncLogEntry
import com.shelldocs.core.domain.entity.source.SyncOutcome
import kotlinx.datetime.Instant

/**
 * Deterministic demo dataset mirroring the original ShellEnterpriseDoc mock
 * sources, so every screen renders real content without a backend.
 */
object DemoSeed {

    private val parser = MarkdownParser()

    val now: Instant = Instant.parse("2026-06-11T10:14:00Z")

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
            updatedAt = Instant.parse("2026-06-08T09:00:00Z"),
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

    val sources: List<KnowledgeSource> = listOf(
        KnowledgeSource(
            id = "source-confluence",
            kind = SourceKind.CONFLUENCE,
            host = "shell-engineering.atlassian.net",
            status = SourceStatus.CONNECTED,
            importedDocs = 84,
            lastSyncAt = Instant.parse("2026-06-11T08:14:00Z"),
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

    val syncLog: List<SyncLogEntry> = listOf(
        SyncLogEntry("log-1", SourceKind.CONFLUENCE, SyncOutcome.SUCCESS, "Confluence sync completed", 3, 7, Instant.parse("2026-06-11T10:14:00Z")),
        SyncLogEntry("log-2", SourceKind.AZURE_DEVOPS, SyncOutcome.SUCCESS, "Azure DevOps sync completed", 0, 2, Instant.parse("2026-06-11T08:02:00Z")),
        SyncLogEntry("log-3", SourceKind.JIRA, SyncOutcome.FAILURE, "Jira sync failed — token expired", 0, 0, Instant.parse("2026-06-09T23:30:00Z")),
        SyncLogEntry("log-4", SourceKind.CONFLUENCE, SyncOutcome.SUCCESS, "Confluence sync completed", 1, 4, Instant.parse("2026-06-09T10:00:00Z")),
        SyncLogEntry("log-5", SourceKind.AZURE_DEVOPS, SyncOutcome.SUCCESS, "Azure DevOps sync completed", 2, 0, Instant.parse("2026-06-09T08:00:00Z")),
    )

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
        updatedAt: Instant,
        team: String = owner.team,
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
            ),
            createdAt = Instant.parse("2025-11-03T00:00:00Z"),
            updatedAt = updatedAt,
        )
    }
}
