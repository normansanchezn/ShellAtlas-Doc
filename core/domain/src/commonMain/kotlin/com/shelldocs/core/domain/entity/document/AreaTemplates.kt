package com.shelldocs.core.domain.entity.document

/** Starter markdown skeleton per [Area] — prefills new documents with the structure the platform expects. */
object AreaTemplates {

    fun forArea(area: Area): String = templates[area] ?: genericTemplate(area)

    private val templates: Map<Area, String> = mapOf(
        Area.BUSINESS to """
            # Business Overview

            ## Context
            Why this matters to the business right now.

            ## Goals
            - Goal 1
            - Goal 2

            ## Stakeholders
            - Owner:
            - Reviewers:

            ## Success Metrics
            - Metric 1
        """.trimIndentMarkdown(),

        Area.DEVELOPMENT to """
            # Technical Overview

            ## Summary
            What this is and why it exists.

            ## Architecture
            Key components and how they interact.

            ## Setup
            ```
            steps to run this locally
            ```

            ## Known Limitations
            - Limitation 1
        """.trimIndentMarkdown(),

        Area.QA to """
            # Test Plan

            ## Scope
            What's being tested and what's out of scope.

            ## Test Cases
            | # | Scenario | Expected Result | Status |
            |---|----------|------------------|--------|
            | 1 |          |                  |        |

            ## Risks
            - Risk 1

            ## Sign-off
            - QA Owner:
        """.trimIndentMarkdown(),

        Area.DESIGN to """
            # Design Spec

            ## Problem
            What user/UX problem this addresses.

            ## Solution
            Key screens/flows (link Figma here).

            ## Interaction Notes
            - Note 1

            ## Open Questions
            - Question 1
        """.trimIndentMarkdown(),

        Area.OWNERS to """
            # Decision Record

            ## Decision
            What was decided.

            ## Context
            Why this decision was needed.

            ## Alternatives Considered
            - Alternative 1

            ## Owner
            -
        """.trimIndentMarkdown(),

        Area.PRODUCT to """
            # Product Requirements

            ## Problem Statement
            What problem this solves for the user.

            ## Requirements
            - Requirement 1

            ## Out of Scope
            - Item 1

            ## Acceptance Criteria
            - Criterion 1
        """.trimIndentMarkdown(),

        Area.MANAGEMENT to """
            # Management Summary

            ## Status
            On track / At risk / Blocked.

            ## Key Updates
            - Update 1

            ## Risks & Blockers
            - Risk 1

            ## Next Steps
            - Step 1
        """.trimIndentMarkdown(),
    )

    private fun genericTemplate(area: Area): String = """
        # ${area.displayName}

        ## Summary

        ## Details

        ## Owner
    """.trimIndentMarkdown()

    private fun String.trimIndentMarkdown(): String = trimIndent().trim()
}
