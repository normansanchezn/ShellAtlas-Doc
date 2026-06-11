---
title: "ADR-004 KMM Single Source of Truth"
type: "decision"
status: "accepted"
created: 2026-06-11
tags:
  - adr
  - shelldoc
  - kmm
---

# ADR-004 KMM Single Source of Truth

## Context

Two recovery commits introduced a second product tree in Swift while the repo already had a KMM application. That left duplicated domain, data, presentation and documentation structures in the same branch.

## Decision

Keep the KMM codebase as the only product implementation. Treat the recovered Swift tree as a temporary source for:

- reusable business rules
- enriched mock terminology and data
- documentation content worth preserving

Do not keep the recovered Swift app as a parallel product tree.

## Consequences

- `core/*`, `feature/*`, `composeApp`, `iosApp`, `backend` and Gradle remain primary.
- `DS-Core`, `SD-*`, `ShellDoc.xcworkspace` and `ShellDoc/` are removed from the main repo flow.
- Any useful recovered behavior must be ported into KMM modules.

## Alternatives Considered

- Keep both product trees in parallel.
- Switch the repo back to the recovered Swift implementation.
- Preserve the Swift tree indefinitely as reference code.
