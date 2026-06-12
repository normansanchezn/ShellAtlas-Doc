---
title: "Dev Log - Runtime Configuration And Release Bundles"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - environment
  - release
---

# Dev Log - Runtime Configuration And Release Bundles

## What changed

- Added a shared `AppEnvironment` contract with `DEV` and `PROD`.
- Unified runtime config loading across Android, Desktop, iOS and Web.
- Added Android `dev` and `prod` flavors with profile-specific `BuildConfig` values.
- Added a root `.env.example` for local setup and release placeholders.

## Files created

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppEnvironment.kt`
- `composeApp/src/iosMain/kotlin/com/shelldocs/app/IosAppConfig.kt`
- `composeApp/src/wasmJsMain/kotlin/com/shelldocs/app/WebAppConfig.kt`
- `.env.example`
- `obsidian-vault/02-architecture/Runtime Configuration and Release Bundles.md`
- `obsidian-vault/06-decisions/ADR-006 Runtime Configuration and Release Profiles.md`
- `obsidian-vault/07-dev-log/Runtime Configuration And Release Bundles.md`

## Files modified

- `composeApp/build.gradle.kts`
- `composeApp/src/commonMain/kotlin/com/shelldocs/app/di/AppConfig.kt`
- `composeApp/src/androidMain/kotlin/com/shelldocs/app/AndroidAppConfig.kt`
- `composeApp/src/desktopMain/kotlin/com/shelldocs/app/DesktopAppConfig.kt`
- `composeApp/src/iosMain/kotlin/com/shelldocs/app/MainViewController.kt`
- `composeApp/src/desktopMain/kotlin/com/shelldocs/app/main.kt`
- `composeApp/src/wasmJsMain/kotlin/com/shelldocs/app/main.kt`
- `docs/project-tree.md`
- `README.md`
- `obsidian-vault/02-architecture/KMM Architecture Overview.md`

## Decisions made

- Use Gradle flavors only where Gradle owns the target lifecycle: Android.
- Keep iOS, Desktop and Web on runtime environment injection instead of fake Gradle flavors.
- Normalize the app contract around `DEV` and `PROD` rather than one-off platform names.
- Fail fast in `PROD` when neither API nor Supabase configuration is present.

## Issues found

- Web bundles cannot rely on process env; they need runtime injection.
- iOS release packaging still depends on Xcode scheme/archive configuration.

## Tests added

- No new unit tests were required for the config contract itself.

## Next steps

- Validate the Android flavor tasks and desktop/web loaders with a build.
- Decide whether production bundles should fail fast when backend URLs are missing.
