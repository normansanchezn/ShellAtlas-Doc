---
title: "Auth Theme Background Flow"
type: "diagram"
status: "active"
created: 2026-06-20
updated: 2026-06-20
tags:
  - shellatlas
  - auth
  - theming
---

# Auth Theme Background Flow

```mermaid
flowchart TD
    A[ThemePreferences.load] --> B[App isDarkTheme state]
    B --> C[SignInScreen]
    C --> D{Dark theme?}
    D -->|Yes| E[Animated node graph background]
    D -->|No| F[Warm light background with glows and contour bands]
    C --> G[Mobile and desktop auth content]
    B --> G
```
