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

    private fun detectType(question: String, document: Document): DiagramType {
        val normalized = "$question ${document.title} ${document.summary}".lowercase()
        return when {
            TIMELINE_MARKERS.any { it in normalized } -> DiagramType.GANTT
            INTERACTION_MARKERS.any { it in normalized } -> DiagramType.SEQUENCE
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
        appendLine("    participant ShellDoc as ShellDoc")
        appendLine("    participant Source as $sourceLabel")
        sections.take(MAX_STEPS).forEach { section ->
            val summary = label(section.steps.firstOrNull() ?: section.heading)
            appendLine("    User->>ShellDoc: ${label(section.heading)}")
            appendLine("    ShellDoc->>Source: $summary")
            appendLine("    Source-->>ShellDoc: Result ready")
            appendLine("    ShellDoc-->>User: ${label(section.heading)} completed")
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
        "release", "timeline", "schedule", "calendar", "phase", "rollout", "pilot", "qa", "cutoff",
    )

    private val INTERACTION_MARKERS = listOf(
        "auth", "authentication", "token", "api", "request", "response", "integration", "secret",
        "sync", "assistant", "service", "webhook",
    )

    private const val MAX_STEPS = 5
}
