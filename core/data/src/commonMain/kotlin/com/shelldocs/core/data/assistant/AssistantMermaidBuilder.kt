package com.shelldocs.core.data.assistant

import com.shelldocs.core.domain.entity.document.Document

internal object AssistantMermaidBuilder {

    internal enum class DiagramType {
        FLOWCHART,
        SEQUENCE,
        GANTT,
    }

    internal data class Section(
        val heading: String,
        val steps: List<String>,
    )

    fun build(question: String, document: Document, sections: List<Section>): String? {
        if (sections.isEmpty()) return null
        return when (detectType(question, document)) {
            DiagramType.FLOWCHART -> flowchart(sections)
            DiagramType.SEQUENCE -> sequenceDiagram(document, sections)
            DiagramType.GANTT -> gantt(document, sections)
        }
    }

    /** Syntax rules for the detected diagram type, written for an LLM prompt — matches exactly what [MermaidParser]-equivalent UI rendering can parse. */
    fun promptHint(question: String, document: Document): String {
        val syntax = when (detectType(question, document)) {
            DiagramType.FLOWCHART -> """
                ```mermaid
                flowchart TD
                    S1[First step]
                    S2[Second step]
                    S1 --> S2
                ```
            """.trimIndent()

            DiagramType.SEQUENCE -> """
                ```mermaid
                sequenceDiagram
                    participant User as User
                    participant ShellAtlas as ShellAtlas
                    User->>ShellAtlas: Action
                    ShellAtlas-->>User: Result
                ```
            """.trimIndent()

            DiagramType.GANTT -> """
                ```mermaid
                gantt
                    title Title
                    dateFormat  YYYY-MM-DD
                    section Flow
                    First step :task0, 2026-06-10, 2d
                ```
            """.trimIndent()
        }
        return "Include exactly one diagram using this Mermaid syntax (keep the node IDs like S1/S2, " +
                "replace bracketed labels with real steps from the documentation, add as many steps as needed):\n$syntax"
    }

    internal fun detectType(question: String, document: Document): DiagramType {
        val normalizedQuestion = question.lowercase()
        val normalizedDocument = "${document.title} ${document.summary}".lowercase()
        return when {
            FLOW_MARKERS.any { it in normalizedQuestion } -> DiagramType.FLOWCHART
            TIMELINE_MARKERS.any { it in normalizedQuestion } -> DiagramType.GANTT
            INTERACTION_MARKERS.any { it in normalizedQuestion } -> DiagramType.SEQUENCE
            INTERACTION_MARKERS.any { it in normalizedDocument } -> DiagramType.SEQUENCE
            TIMELINE_MARKERS.any { it in normalizedDocument } -> DiagramType.GANTT
            else -> DiagramType.FLOWCHART
        }
    }

    private fun flowchart(sections: List<Section>): String = buildString {
        appendLine("```mermaid")
        appendLine("flowchart TD")
        val nodes = sections.take(MAX_STEPS).mapIndexed { index, section ->
            "S${index + 1}" to label(section.heading.ifBlank { section.steps.firstOrNull() ?: "Step ${index + 1}" })
        }
        nodes.forEach { (id, text) -> appendLine("    $id[$text]") }
        nodes.zipWithNext().forEach { (from, to) -> appendLine("    ${from.first} --> ${to.first}") }
        appendLine("```")
    }.trim()

    private fun sequenceDiagram(document: Document, sections: List<Section>): String = buildString {
        val sourceLabel = label(
            document.attributes.module.ifBlank {
                document.attributes.platform.ifBlank { "Platform Service" }
            },
        )
        appendLine("```mermaid")
        appendLine("sequenceDiagram")
        appendLine("    participant User as User")
        appendLine("    participant ShellAtlas as ShellAtlas")
        appendLine("    participant Source as $sourceLabel")
        sections.take(MAX_STEPS).forEach { section ->
            val summary = label(section.steps.firstOrNull() ?: section.heading)
            appendLine("    User->>ShellAtlas: ${label(section.heading)}")
            appendLine("    ShellAtlas->>Source: $summary")
            appendLine("    Source-->>ShellAtlas: Result ready")
            appendLine("    ShellAtlas-->>User: ${label(section.heading)} completed")
        }
        appendLine("```")
    }.trim()

    private fun gantt(document: Document, sections: List<Section>): String = buildString {
        appendLine("```mermaid")
        appendLine("gantt")
        appendLine("    title ${label(document.title)}")
        appendLine("    dateFormat  YYYY-MM-DD")
        appendLine("    axisFormat  %d/%m")
        appendLine("    section Flow")
        sections.take(MAX_STEPS).forEachIndexed { index, section ->
            val startDay = 10 + index * 2
            val duration = if (section.steps.size >= 3) 2 else 1
            appendLine("    ${label(section.heading)} :task$index, 2026-06-$startDay, ${duration}d")
        }
        appendLine("```")
    }.trim()

    private fun label(raw: String): String = raw
        .replace("\"", "'")
        .replace("[", "(")
        .replace("]", ")")
        .replace("{", "(")
        .replace("}", ")")
        .trim()
        .ifBlank { "Step" }
        .take(42)

    private val TIMELINE_MARKERS = listOf(
        "timeline", "schedule", "calendar", "phase", "rollout", "cutoff", "roadmap", "milestone",
        "cronograma", "calendario", "fechas", "fase", "fases", "hitos", "linea de tiempo",
    )

    private val INTERACTION_MARKERS = listOf(
        "auth", "authentication", "token", "api", "request", "response", "integration", "secret",
        "sync", "assistant", "service", "webhook",
    )

    private val FLOW_MARKERS = listOf(
        "flow", "workflow", "process", "step by step", "how it works",
        "flujo", "proceso", "paso a paso", "como funciona", "cómo funciona",
    )

    private const val MAX_STEPS = 5
}
