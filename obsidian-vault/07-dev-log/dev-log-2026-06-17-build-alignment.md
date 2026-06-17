---
title: "Dev Log - Android Build Alignment"
type: "dev-log"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - dev-log
  - android
  - build
---

# Dev Log - Android Build Alignment

## What changed

Pinned every Android Kotlin Multiplatform module to JVM target 11 to match `composeApp`.

## Files created

- `obsidian-vault/03-features/android-build-alignment.md`
- `obsidian-vault/08-diagrams/android-build-alignment-flow.md`
- `obsidian-vault/07-dev-log/dev-log-2026-06-17-build-alignment.md`

## Files modified

- `docs/project-tree.md`
- `core/common/build.gradle.kts`
- `core/domain/build.gradle.kts`
- `core/data/build.gradle.kts`
- `core/designsystem/build.gradle.kts`
- `feature/assistant/build.gradle.kts`
- `feature/auth/build.gradle.kts`
- `feature/dashboard/build.gradle.kts`
- `feature/documents/build.gradle.kts`
- `feature/settings/build.gradle.kts`
- `feature/sources/build.gradle.kts`
- `feature/updates/build.gradle.kts`

## Decisions made

- Kept current library versions because the breakage was caused by JVM target drift, not by incompatible dependency versions.
- Preferred a minimal fix over a broader JVM 17 migration.

## Issues found

- `GRADLE_USER_HOME` points outside the workspace in this Codex environment, which can fail inside sandboxed runs.
- Android source-set deprecation warning still exists for `composeApp/src/androidTest/kotlin`.

## Tests added

- No new automated tests.
- Verified with Android and iOS build commands.

## Next steps

- Consider extracting shared Android compiler settings into a common Gradle convention.
- Consider migrating from JVM 11 to JVM 17 across Android modules in one coordinated change.
