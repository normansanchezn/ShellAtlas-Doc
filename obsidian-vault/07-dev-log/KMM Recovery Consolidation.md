---
title: "KMM Recovery Consolidation"
type: "dev-log"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - dev-log
  - kmm
---

# KMM Recovery Consolidation

## What changed

- Declared KMM as the only implementation to keep.
- Rebuilt the Obsidian vault for the KMM structure.
- Updated the project tree to remove the recovered Swift duplicate from the target architecture.
- Added desktop `.env` loading for local Supabase/API configuration.
- Clarified demo sign-in behavior in the auth UI and README.

## Decisions made

- Recovered Swift logic may be ported selectively into KMM.
- Recovered Swift UI will not stay as a maintained second app.

## Next steps

- Port search aliases and other reusable heuristics into KMM.
- Remove duplicated Swift package and Xcode product trees from the repository.
