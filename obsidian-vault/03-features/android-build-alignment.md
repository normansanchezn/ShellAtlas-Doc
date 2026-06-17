---
title: "Android Build Alignment"
type: "feature"
status: "active"
platform: "Android/iOS/macOS/Web"
area: "ShellAtlas"
owner: "Product Engineering"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - build
  - kotlin
  - android
---

# Android Build Alignment

## Summary

Aligned all Android Kotlin Multiplatform modules to the same JVM target used by `composeApp`.

## Purpose

Prevent Android compilation failures caused by inline bytecode generated with a newer JVM target in library modules.

## User Problem

Android builds failed while iOS shared compilation still succeeded, making the project look partially broken.

## Expected Behavior

All Android-facing modules compile with the same bytecode target so inline functions can be consumed safely across modules.

## Related Files

- `composeApp/build.gradle.kts`
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

## Domain Models

- None

## UseCases

- None

## Repositories

- `AppAuthRepository` was the call site where the mismatch surfaced.

## ViewModels

- None

## SwiftUI Views

- `iosApp` remains a host only. No SwiftUI view changes were required.

## Atomic Design Components Used

- None

## Mock Data Used

- None

## Data Flow

Android app module compiles against shared feature and core modules. When one module emits newer JVM bytecode than the app target, inline calls fail during compilation.

## Mermaid Diagram

See `obsidian-vault/08-diagrams/android-build-alignment-flow.md`.

## Development Notes

The root cause was not outdated libraries. The current stack was already modern enough; the issue was inconsistent JVM target configuration between `composeApp` and Android library modules.

## Open Questions

- Whether the project should move the whole Android stack from JVM 11 to JVM 17 in a dedicated follow-up.
- Whether shared Gradle convention logic should replace the repeated per-module configuration.
