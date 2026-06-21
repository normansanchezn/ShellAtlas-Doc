---
title: "Brand Asset Distribution Flow"
type: "diagram"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shelldoc
  - diagram
  - branding
---

# Brand Asset Distribution Flow

```mermaid
flowchart LR
    A[shell_atlas_icon.svg] --> B[WorkspaceSidebar]
    A --> C[WorkspaceRail]
    A --> D[ShellAtlas.icns]
    E[shell_atlas_logo.svg] --> B
    D --> F[macOS nativeDistributions]
    F --> G[Installed desktop app icon]
```
