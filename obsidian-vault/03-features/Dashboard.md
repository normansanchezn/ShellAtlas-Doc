---
title: "Dashboard"
type: "feature"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-17
updated: 2026-06-17
tags:
  - shelldoc
  - dashboard
  - mobile
---

# Dashboard

## Summary

Operational overview screen for ShellDoc metrics, health summaries and activity trends. On phones it now prioritizes density over horizontal waste by using a 3-column metrics grid and a 2x2 card layout for chart-heavy sections.

## Related Files

- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/DashboardScreen.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/KnowledgeHealthCard.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/StatusDonutCard.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/ModuleCoverageCard.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/UsageChartCard.kt`
- `feature/dashboard/src/commonMain/kotlin/com/shelldocs/feature/dashboard/ui/RecentActivityCard.kt`

## Expected Behavior

- Desktop and tablet keep the broader analytics composition.
- Phone layouts collapse metric cards into a 3-column grid.
- Phone chart cards render in a balanced 2x2 arrangement before recent activity.
- No additional behavior changes were introduced outside the mobile density update.

## Data Flow

`DashboardScreen` -> `MetricCards(columns = 3)` on narrow layouts -> paired mobile chart rows -> recent activity list.

## Mermaid Diagram

- `obsidian-vault/08-diagrams/Dashboard Mobile Layout Flow.md`

## Development Notes

- The update is intentionally layout-only; card content and calculations remain unchanged.
- Empty cells in the final metrics row are padded to preserve alignment on narrow screens.
