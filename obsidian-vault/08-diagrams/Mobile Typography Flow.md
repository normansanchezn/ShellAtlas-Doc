---
title: "Mobile Typography Flow"
type: "diagram"
status: "active"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - typography
  - mobile
  - diagram
---

# Mobile Typography Flow

```mermaid
flowchart TD
    A["App width check"] --> B{"width < mobile threshold?"}
    B -->|Yes| C["ShellTypography.mobile()"]
    B -->|No| D["ShellTypography.default()"]
    C --> E["ShellDocsTheme"]
    D --> E
    E --> F["Accessible text across mobile screens"]
```
