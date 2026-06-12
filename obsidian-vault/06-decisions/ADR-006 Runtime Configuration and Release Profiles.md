---
title: "ADR-006 Runtime Configuration and Release Profiles"
type: "decision"
status: "accepted"
created: 2026-06-12
tags:
  - adr
  - shelldoc
  - environment
  - release
---

# ADR-006 Runtime Configuration and Release Profiles

## Context

ShellDoc needs the same app codebase to support local development and release bundles across Android, iOS, Desktop and Web.

## Decision

Introduce a shared `AppEnvironment` contract with `DEV` and `PROD`, generate Android `dev` and `prod` flavors in Gradle, and keep platform-specific loaders for Desktop, iOS and Web.

## Consequences

- Release configuration becomes explicit and reproducible.
- Local development can keep using `.env` files and demo data.
- Android gets first-class bundle variants.
- Desktop, iOS and Web can still be packaged with platform-specific env injection.
- `PROD` builds will surface missing backend configuration immediately instead of silently falling back to demo mode.

## Alternatives Considered

- Hardcoding one config per target: rejected because it does not scale across environments.
- Keeping config only in `.env`: rejected because Android release bundles need Gradle-backed variants.
- Introducing a remote config service: rejected for the MVP.
