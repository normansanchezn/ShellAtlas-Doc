import SwiftUI
import SD_Domain

public struct SDMarkdownBodyView: View {
    private let content: String
    private let suppressInitialHeading: String?
    private let textColor: Color
    private let secondaryTextColor: Color

    public init(
        content: String,
        suppressInitialHeading: String? = nil,
        textColor: Color = SDColors.textPrimary,
        secondaryTextColor: Color = SDColors.textSecondary
    ) {
        self.content = content
        self.suppressInitialHeading = suppressInitialHeading
        self.textColor = textColor
        self.secondaryTextColor = secondaryTextColor
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ForEach(Array(parsedBlocks.enumerated()), id: \.offset) { _, block in
                SDMarkdownBlockView(
                    block: block,
                    textColor: textColor,
                    secondaryTextColor: secondaryTextColor
                )
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var parsedBlocks: [SDMarkdownBlock] {
        var blocks = SDMarkdownParser.parse(content)
        guard
            let suppressInitialHeading,
            let first = blocks.first,
            first.headingText?.caseInsensitiveCompare(suppressInitialHeading) == .orderedSame
        else {
            return blocks
        }
        blocks.removeFirst()
        return blocks
    }
}

private enum SDMarkdownBlock {
    case h1(String)
    case h2(String)
    case h3(String)
    case paragraph(String)
    case codeBlock(lang: String, code: String)
    case checklistItem(checked: Bool, text: String)
    case bulletItem(indent: Int, text: String)
    case horizontalRule
    case tableRaw([String])
    case empty

    var headingText: String? {
        switch self {
        case .h1(let text), .h2(let text), .h3(let text):
            text
        default:
            nil
        }
    }
}

private struct SDMarkdownBlockView: View {
    let block: SDMarkdownBlock
    let textColor: Color
    let secondaryTextColor: Color

    var body: some View {
        switch block {
        case .h1(let text):
            Text(text)
                .font(.title.weight(.bold))
                .foregroundStyle(textColor)
                .padding(.top, 16)
                .padding(.bottom, 4)
                .frame(maxWidth: .infinity, alignment: .leading)

        case .h2(let text):
            VStack(alignment: .leading, spacing: 5) {
                Text(text)
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(textColor)
                Rectangle()
                    .fill(SDColors.border.opacity(0.75))
                    .frame(height: 1)
            }
            .padding(.top, 14)
            .padding(.bottom, 4)

        case .h3(let text):
            Text(text)
                .font(.headline.weight(.semibold))
                .foregroundStyle(textColor)
                .padding(.top, 10)
                .padding(.bottom, 2)
                .frame(maxWidth: .infinity, alignment: .leading)

        case .paragraph(let text):
            VStack(alignment: .leading, spacing: 6) {
                inlineText(text)
                    .font(.body)
                    .foregroundStyle(textColor)
                    .textSelection(.enabled)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)

                SDReferenceLinksView(text: text)
            }
            .padding(.vertical, 2)

        case .codeBlock(let lang, let code):
            if lang.lowercased() == "mermaid" {
                SDMermaidDiagramView(source: code)
                    .padding(.vertical, 8)
            } else {
                SDCodeBlockView(language: lang, code: code)
                    .padding(.vertical, 6)
            }

        case .checklistItem(let checked, let text):
            HStack(alignment: .top, spacing: 8) {
                Image(systemName: checked ? "checkmark.square.fill" : "square")
                    .font(.body)
                    .foregroundStyle(checked ? SDColors.success : secondaryTextColor)
                    .padding(.top, 1)
                VStack(alignment: .leading, spacing: 5) {
                    inlineText(text)
                        .font(.body)
                        .foregroundStyle(checked ? secondaryTextColor : textColor)
                        .strikethrough(checked, color: secondaryTextColor)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    SDReferenceLinksView(text: text)
                }
            }
            .padding(.vertical, 1)

        case .bulletItem(let indent, let text):
            HStack(alignment: .top, spacing: 8) {
                Text(indent == 0 ? "•" : "◦")
                    .font(.body)
                    .foregroundStyle(secondaryTextColor)
                    .padding(.top, 1)
                VStack(alignment: .leading, spacing: 5) {
                    inlineText(text)
                        .font(.body)
                        .foregroundStyle(textColor)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    SDReferenceLinksView(text: text)
                }
            }
            .padding(.leading, CGFloat(indent) * 16)
            .padding(.vertical, 1)

        case .horizontalRule:
            Rectangle()
                .fill(SDColors.border.opacity(0.75))
                .frame(height: 1)
                .padding(.vertical, 8)

        case .tableRaw(let rows):
            SDMarkdownTableView(rows: rows)
                .padding(.vertical, 6)

        case .empty:
            Color.clear.frame(height: 6)
        }
    }

    @ViewBuilder
    private func inlineText(_ raw: String) -> some View {
        if let attributed = try? AttributedString(
            markdown: raw,
            options: .init(interpretedSyntax: .inlineOnlyPreservingWhitespace)
        ) {
            Text(attributed)
        } else {
            Text(raw)
        }
    }
}

private struct SDCodeBlockView: View {
    let language: String
    let code: String

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if !language.isEmpty {
                Text(language.uppercased())
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(SDColors.secondary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(SDColors.secondary.opacity(0.08))
            }

            ScrollView(.horizontal, showsIndicators: true) {
                Text(code)
                    .font(.system(.callout, design: .monospaced))
                    .foregroundStyle(SDColors.textPrimary)
                    .textSelection(.enabled)
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .background(SDColors.background.opacity(0.84), in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.9), lineWidth: 1)
        }
    }
}

private struct SDMermaidDiagramView: View {
    let source: String

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label(diagramLabel, systemImage: "point.3.connected.trianglepath.dotted")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(SDColors.secondary)
                Spacer()
            }
            diagramContent
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.regularMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.secondary.opacity(0.28), lineWidth: 1)
        }
    }

    private var diagramLabel: String {
        switch SDMermaidParser.detectType(from: source) {
        case .sequence: "Sequence"
        case .flowTD, .flowLR, .unknown: "Flow"
        case .state: "States"
        case .gantt: "Timeline"
        case .pie: "Distribution"
        }
    }

    @ViewBuilder
    private var diagramContent: some View {
        switch SDMermaidParser.detectType(from: source) {
        case .sequence:
            sequenceOrFallback
        case .flowTD:
            verticalFlowView(SDMermaidParser.flowLabels(from: source))
        case .state:
            stateOrFallback
        case .gantt:
            ganttView(SDMermaidParser.ganttSections(from: source))
        case .pie:
            pieView(SDMermaidParser.pieData(from: source))
        case .flowLR, .unknown:
            horizontalFlowView(SDMermaidParser.flowLabels(from: source))
        }
    }

    @ViewBuilder
    private var sequenceOrFallback: some View {
        if let messages = SDMermaidParser.sequenceMessages(from: source), !messages.isEmpty {
            sequenceView(messages)
        } else {
            horizontalFlowView([])
        }
    }

    @ViewBuilder
    private var stateOrFallback: some View {
        let transitions = SDMermaidParser.stateTransitions(from: source)
        if transitions.isEmpty {
            horizontalFlowView(SDMermaidParser.flowLabels(from: source))
        } else {
            sequenceView(transitions)
        }
    }

    private func horizontalFlowView(_ labels: [String]) -> some View {
        let usableLabels = labels.isEmpty ? source.nonEmptyLines.prefix(8).map { $0 } : labels
        return ScrollView(.horizontal, showsIndicators: usableLabels.count > 4) {
            HStack(alignment: .center, spacing: 10) {
                ForEach(Array(usableLabels.enumerated()), id: \.offset) { index, label in
                    Text(label)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.textPrimary)
                        .multilineTextAlignment(.center)
                        .lineLimit(4)
                        .minimumScaleFactor(0.78)
                        .frame(width: 140)
                        .frame(minHeight: 56)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 9)
                        .background(SDColors.surface.opacity(0.72), in: .rect(cornerRadius: 8))
                        .overlay {
                            RoundedRectangle(cornerRadius: 8)
                                .strokeBorder(SDColors.secondary.opacity(0.22), lineWidth: 1)
                        }
                    if index < usableLabels.count - 1 {
                        Image(systemName: "arrow.right")
                            .font(.caption.weight(.bold))
                            .foregroundStyle(SDColors.primary)
                    }
                }
            }
            .padding(.vertical, 2)
        }
    }

    private func verticalFlowView(_ labels: [String]) -> some View {
        let usableLabels = labels.isEmpty ? source.nonEmptyLines.prefix(8).map { $0 } : labels
        return VStack(alignment: .center, spacing: 0) {
            ForEach(Array(usableLabels.prefix(10).enumerated()), id: \.offset) { index, label in
                Text(label)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                    .multilineTextAlignment(.center)
                    .lineLimit(3)
                    .minimumScaleFactor(0.78)
                    .frame(maxWidth: 280)
                    .frame(minHeight: 44)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 9)
                    .background(SDColors.surface.opacity(0.72), in: .rect(cornerRadius: 8))
                    .overlay {
                        RoundedRectangle(cornerRadius: 8)
                            .strokeBorder(SDColors.secondary.opacity(0.22), lineWidth: 1)
                    }
                if index < usableLabels.count - 1 {
                    VStack(spacing: 0) {
                        Rectangle()
                            .fill(SDColors.primary.opacity(0.5))
                            .frame(width: 2, height: 10)
                        Image(systemName: "arrowtriangle.down.fill")
                            .font(.system(size: 8, weight: .bold))
                            .foregroundStyle(SDColors.primary)
                        Rectangle()
                            .fill(SDColors.primary.opacity(0.5))
                            .frame(width: 2, height: 2)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
    }

    private func sequenceView(_ messages: [String]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            ForEach(Array(messages.prefix(12).enumerated()), id: \.offset) { index, message in
                HStack(alignment: .top, spacing: 9) {
                    Text("\(index + 1)")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(SDColors.background)
                        .frame(width: 22, height: 22)
                        .background(SDColors.primary, in: .circle)
                    Text(message)
                        .font(.caption.weight(.medium))
                        .foregroundStyle(SDColors.textPrimary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
    }

    private func ganttView(_ sections: [(name: String, tasks: [String])]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            if sections.isEmpty {
                Text("Timeline diagram")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            } else {
                ForEach(sections.prefix(6), id: \.name) { section in
                    VStack(alignment: .leading, spacing: 4) {
                        Label(section.name, systemImage: "calendar")
                            .font(.caption.weight(.bold))
                            .foregroundStyle(SDColors.primary)
                        ForEach(section.tasks.prefix(8), id: \.self) { task in
                            HStack(spacing: 6) {
                                Image(systemName: "chevron.right")
                                    .font(.caption2.weight(.semibold))
                                    .foregroundStyle(SDColors.secondary)
                                Text(task)
                                    .font(.caption)
                                    .foregroundStyle(SDColors.textPrimary)
                            }
                            .padding(.leading, 8)
                        }
                    }
                }
            }
        }
    }

    private func pieView(_ data: [(label: String, value: Double)]) -> some View {
        let total = data.map(\.value).reduce(0, +)
        let colors: [Color] = [SDColors.primary, SDColors.secondary, SDColors.success, SDColors.warning, SDColors.danger, SDColors.primary.opacity(0.5)]
        return VStack(alignment: .leading, spacing: 7) {
            if data.isEmpty {
                Text("Distribution diagram")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            } else {
                ForEach(Array(data.prefix(8).enumerated()), id: \.offset) { index, item in
                    VStack(alignment: .leading, spacing: 3) {
                        HStack {
                            Circle()
                                .fill(colors[index % colors.count])
                                .frame(width: 8, height: 8)
                            Text(item.label)
                                .font(.caption.weight(.medium))
                                .foregroundStyle(SDColors.textPrimary)
                            Spacer()
                            Text(total > 0 ? "\(Int(item.value / total * 100))%" : "\(Int(item.value))")
                                .font(.caption2.weight(.semibold))
                                .foregroundStyle(SDColors.textSecondary)
                        }
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                Rectangle()
                                    .fill(SDColors.border.opacity(0.3))
                                Rectangle()
                                    .fill(colors[index % colors.count].opacity(0.72))
                                    .frame(width: total > 0 ? geo.size.width * (item.value / total) : geo.size.width * 0.5)
                            }
                            .clipShape(.rect(cornerRadius: 3))
                        }
                        .frame(height: 6)
                    }
                }
            }
        }
    }
}

private struct SDMarkdownTableView: View {
    let rows: [String]

    private var parsedRows: [[String]] {
        rows.compactMap(Self.parseRow).filter { !$0.isSeparatorRow }
    }

    private var columnCount: Int {
        parsedRows.map(\.count).max() ?? 0
    }

    var body: some View {
        if parsedRows.isEmpty {
            EmptyView()
        } else {
            ScrollView(.horizontal, showsIndicators: columnCount > 4) {
                VStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(parsedRows.enumerated()), id: \.offset) { rowIndex, cells in
                        HStack(spacing: 0) {
                            ForEach(0..<columnCount, id: \.self) { columnIndex in
                                inlineText(cells[safe: columnIndex] ?? "")
                                    .font(rowIndex == 0 ? .caption.weight(.semibold) : .caption)
                                    .foregroundStyle(SDColors.textPrimary)
                                    .lineLimit(nil)
                                    .fixedSize(horizontal: false, vertical: true)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 7)
                                    .frame(minWidth: 124, maxWidth: 220, alignment: .leading)
                                    .background(rowBackground(rowIndex))
                                    .overlay(alignment: .trailing) {
                                        Rectangle()
                                            .fill(SDColors.border.opacity(0.55))
                                            .frame(width: 1)
                                    }
                            }
                        }
                        .overlay(alignment: .bottom) {
                            Rectangle()
                                .fill(SDColors.border.opacity(0.55))
                                .frame(height: 1)
                        }
                    }
                }
                .background(SDColors.background.opacity(0.38), in: .rect(cornerRadius: 8))
                .overlay {
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(SDColors.border.opacity(0.82), lineWidth: 1)
                }
            }
        }
    }

    @ViewBuilder
    private func inlineText(_ raw: String) -> some View {
        if let attributed = try? AttributedString(
            markdown: raw,
            options: .init(interpretedSyntax: .inlineOnlyPreservingWhitespace)
        ) {
            Text(attributed)
        } else {
            Text(raw)
        }
    }

    private func rowBackground(_ rowIndex: Int) -> Color {
        if rowIndex == 0 {
            SDColors.elevatedSurface.opacity(0.82)
        } else if rowIndex.isMultiple(of: 2) {
            SDColors.elevatedSurface.opacity(0.28)
        } else {
            SDColors.cardBackground.opacity(0.28)
        }
    }

    private static func parseRow(_ row: String) -> [String]? {
        let trimmed = row.trimmingCharacters(in: .whitespaces)
        guard trimmed.hasPrefix("|"), trimmed.hasSuffix("|") else { return nil }
        let cells = trimmed
            .split(separator: "|", omittingEmptySubsequences: false)
            .dropFirst()
            .dropLast()
            .map { $0.trimmingCharacters(in: .whitespaces) }
        return cells.isEmpty ? nil : cells
    }
}

private extension Array where Element == String {
    var isSeparatorRow: Bool {
        guard !isEmpty else { return false }
        return allSatisfy { cell in
            let trimmed = cell.trimmingCharacters(in: .whitespaces)
            guard trimmed.count >= 3 else { return false }
            return trimmed.allSatisfy { $0 == "-" || $0 == ":" }
        }
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

private extension Array where Element == [String] {
    var columnCount: Int {
        map(\.count).max() ?? 0
    }
}

private struct SDReferenceLinksView: View {
    let text: String

    var body: some View {
        let references = SDReferenceDetector.references(in: text)
        if !references.isEmpty {
            FlowLayout(spacing: 6) {
                ForEach(references) { reference in
                    Link(destination: reference.url) {
                        Label(reference.label, systemImage: reference.systemImage)
                            .font(.caption2.weight(.semibold))
                            .foregroundStyle(reference.color)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 5)
                            .background(reference.color.opacity(0.12), in: .capsule)
                    }
                    .buttonStyle(.plain)
                    .help("Open \(reference.label)")
                }
            }
        }
    }
}

private struct SDExternalReference: Identifiable, Hashable {
    let id: String
    let label: String
    let url: URL
    let systemImage: String
    let color: Color
}

private enum SDReferenceDetector {
    static func references(in text: String) -> [SDExternalReference] {
        var seen = Set<String>()
        var references: [SDExternalReference] = []

        for key in matches(pattern: #"\bADO-(\d+)\b"#, text: text) {
            let number = key.replacingOccurrences(of: "ADO-", with: "")
            append(
                SDExternalReference(
                    id: "ado-\(number)",
                    label: key,
                    url: URL(string: "https://dev.azure.com/shell/mobile/_workitems/edit/\(number)")!,
                    systemImage: "tray.full.fill",
                    color: SDColors.secondary
                ),
                seen: &seen,
                references: &references
            )
        }

        for key in matches(pattern: #"\b(?!ADO-)[A-Z][A-Z0-9]+-\d+\b"#, text: text) {
            append(
                SDExternalReference(
                    id: "jira-\(key)",
                    label: key,
                    url: URL(string: "https://jira.internal/browse/\(key)")!,
                    systemImage: "ticket.fill",
                    color: SDColors.primary
                ),
                seen: &seen,
                references: &references
            )
        }

        if text.localizedCaseInsensitiveContains("Azure Work") {
            for number in matches(pattern: #"\b\d{5,}\b"#, text: text) {
                append(
                    SDExternalReference(
                        id: "ado-\(number)",
                        label: "ADO-\(number)",
                        url: URL(string: "https://dev.azure.com/shell/mobile/_workitems/edit/\(number)")!,
                        systemImage: "tray.full.fill",
                        color: SDColors.secondary
                    ),
                    seen: &seen,
                    references: &references
                )
            }
        }

        return references
    }

    private static func append(
        _ reference: SDExternalReference,
        seen: inout Set<String>,
        references: inout [SDExternalReference]
    ) {
        guard !seen.contains(reference.id) else { return }
        seen.insert(reference.id)
        references.append(reference)
    }

    private static func matches(pattern: String, text: String) -> [String] {
        guard let regex = try? NSRegularExpression(pattern: pattern) else { return [] }
        let nsRange = NSRange(text.startIndex..<text.endIndex, in: text)
        return regex.matches(in: text, range: nsRange).compactMap { match in
            guard let range = Range(match.range, in: text) else { return nil }
            return String(text[range])
        }
    }
}

private enum SDMermaidDiagramType {
    case flowTD, flowLR, sequence, state, gantt, pie, unknown
}

private enum SDMermaidParser {
    static func detectType(from source: String) -> SDMermaidDiagramType {
        let lower = source.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if lower.hasPrefix("sequencediagram") { return .sequence }
        if lower.hasPrefix("statediagram") { return .state }
        if lower.hasPrefix("gantt") { return .gantt }
        if lower.hasPrefix("pie") { return .pie }
        if lower.hasPrefix("flowchart td") || lower.hasPrefix("graph td") { return .flowTD }
        if lower.hasPrefix("flowchart lr") || lower.hasPrefix("graph lr") { return .flowLR }
        if lower.hasPrefix("flowchart") || lower.hasPrefix("graph") { return .flowLR }
        return .unknown
    }

    static func flowLabels(from source: String) -> [String] {
        var labels: [String] = []
        for line in source.nonEmptyLines {
            guard line.contains("-->") else { continue }
            labels.append(contentsOf: labelsInBrackets(line))
        }
        return labels.deduplicated()
    }

    static func sequenceMessages(from source: String) -> [String]? {
        guard source.localizedCaseInsensitiveContains("sequenceDiagram") else { return nil }
        return source.nonEmptyLines.compactMap { line in
            guard line.contains("->") || line.contains("-->") else { return nil }
            let parts = line.components(separatedBy: ":")
            guard parts.count > 1 else { return line.trimmingCharacters(in: .whitespaces) }
            return parts.dropFirst().joined(separator: ":").trimmingCharacters(in: .whitespaces)
        }
    }

    static func stateTransitions(from source: String) -> [String] {
        source.components(separatedBy: "\n").compactMap { line in
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            guard trimmed.contains("-->") else { return nil }
            let parts = trimmed.components(separatedBy: "-->")
            let from = parts.first?.trimmingCharacters(in: .whitespaces)
                .replacingOccurrences(of: "[*]", with: "Start")
                .trimmingCharacters(in: .whitespaces) ?? ""
            let rest = parts.dropFirst().joined(separator: "-->").trimmingCharacters(in: .whitespaces)
            let toAndLabel = rest.components(separatedBy: ":")
            let to = toAndLabel.first?.trimmingCharacters(in: .whitespaces)
                .replacingOccurrences(of: "[*]", with: "End")
                .trimmingCharacters(in: .whitespaces) ?? ""
            let label = toAndLabel.count > 1 ? " — \(toAndLabel.dropFirst().joined(separator: ":").trimmingCharacters(in: .whitespaces))" : ""
            guard !from.isEmpty, !to.isEmpty else { return nil }
            return "\(from) → \(to)\(label)"
        }
    }

    static func ganttSections(from source: String) -> [(name: String, tasks: [String])] {
        var sections: [(name: String, tasks: [String])] = []
        var currentSection: String? = nil
        var currentTasks: [String] = []
        for line in source.components(separatedBy: "\n") {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            if trimmed.lowercased().hasPrefix("section ") {
                if let name = currentSection {
                    sections.append((name: name, tasks: currentTasks))
                }
                currentSection = String(trimmed.dropFirst(8))
                currentTasks = []
            } else if trimmed.isEmpty || ["gantt", "title", "dateformat", "axisformat", "excludes"].contains(where: { trimmed.lowercased().hasPrefix($0) }) {
                continue
            } else if let colonIdx = trimmed.firstIndex(of: ":") {
                let taskName = String(trimmed[..<colonIdx]).trimmingCharacters(in: .whitespaces)
                if !taskName.isEmpty { currentTasks.append(taskName) }
            }
        }
        if let name = currentSection { sections.append((name: name, tasks: currentTasks)) }
        if sections.isEmpty, !currentTasks.isEmpty {
            sections.append((name: "Tasks", tasks: currentTasks))
        }
        return sections
    }

    static func pieData(from source: String) -> [(label: String, value: Double)] {
        var result: [(label: String, value: Double)] = []
        guard let regex = try? NSRegularExpression(pattern: #""([^"]+)"\s*:\s*([0-9]+(?:\.[0-9]+)?)"#) else { return result }
        for line in source.components(separatedBy: "\n") {
            let nsRange = NSRange(line.startIndex..<line.endIndex, in: line)
            if let match = regex.firstMatch(in: line, range: nsRange),
               let labelRange = Range(match.range(at: 1), in: line),
               let valueRange = Range(match.range(at: 2), in: line) {
                result.append((label: String(line[labelRange]), value: Double(String(line[valueRange])) ?? 0))
            }
        }
        return result
    }

    private static func labelsInBrackets(_ line: String) -> [String] {
        guard let regex = try? NSRegularExpression(pattern: #"\[([^\]]+)\]|\{([^\}]+)\}"#) else {
            return []
        }
        let nsRange = NSRange(line.startIndex..<line.endIndex, in: line)
        return regex.matches(in: line, range: nsRange).compactMap { match in
            let range = match.range(at: match.range(at: 1).location != NSNotFound ? 1 : 2)
            guard let swiftRange = Range(range, in: line) else { return nil }
            return String(line[swiftRange])
        }
    }
}

private struct FlowLayout<Content: View>: View {
    let spacing: CGFloat
    @ViewBuilder let content: Content

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 92), spacing: spacing, alignment: .leading)], alignment: .leading, spacing: spacing) {
            content
        }
    }
}

private enum SDMarkdownParser {
    static func parse(_ raw: String) -> [SDMarkdownBlock] {
        let lines = raw.components(separatedBy: "\n")
        var blocks: [SDMarkdownBlock] = []
        var i = 0

        while i < lines.count {
            let line = lines[i]
            let trimmed = line.trimmingCharacters(in: .whitespaces)

            if trimmed.hasPrefix("```") {
                let lang = String(trimmed.dropFirst(3)).trimmingCharacters(in: .whitespaces)
                var codeLines: [String] = []
                i += 1
                while i < lines.count && !lines[i].trimmingCharacters(in: .whitespaces).hasPrefix("```") {
                    codeLines.append(lines[i])
                    i += 1
                }
                blocks.append(.codeBlock(lang: lang, code: codeLines.joined(separator: "\n")))
                i += 1
                continue
            }

            if trimmed.hasPrefix("|") {
                var tableLines: [String] = []
                while i < lines.count && lines[i].trimmingCharacters(in: .whitespaces).hasPrefix("|") {
                    tableLines.append(lines[i])
                    i += 1
                }
                blocks.append(.tableRaw(tableLines))
                continue
            }

            if trimmed.hasPrefix("### ") {
                blocks.append(.h3(String(trimmed.dropFirst(4))))
            } else if trimmed.hasPrefix("## ") {
                blocks.append(.h2(String(trimmed.dropFirst(3))))
            } else if trimmed.hasPrefix("# ") {
                blocks.append(.h1(String(trimmed.dropFirst(2))))
            } else if trimmed == "---" || trimmed == "***" || trimmed == "___" {
                blocks.append(.horizontalRule)
            } else if trimmed.hasPrefix("- [ ] ") {
                blocks.append(.checklistItem(checked: false, text: String(trimmed.dropFirst(6))))
            } else if trimmed.hasPrefix("- [x] ") || trimmed.hasPrefix("- [X] ") {
                blocks.append(.checklistItem(checked: true, text: String(trimmed.dropFirst(6))))
            } else if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") || trimmed.hasPrefix("+ ") {
                let indent = line.prefix(while: { $0 == " " || $0 == "\t" }).count / 2
                blocks.append(.bulletItem(indent: indent, text: String(trimmed.dropFirst(2))))
            } else if trimmed.isEmpty {
                blocks.append(.empty)
            } else {
                blocks.append(.paragraph(trimmed))
            }

            i += 1
        }

        return blocks
    }
}

private extension String {
    var nonEmptyLines: [String] {
        let diagramHeaders = ["flowchart", "graph", "sequencediagram", "statediagram", "gantt", "pie", "erdiagram", "timeline", "mindmap"]
        return components(separatedBy: "\n")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { line in
                guard !line.isEmpty else { return false }
                let lower = line.lowercased()
                return !diagramHeaders.contains { lower.hasPrefix($0) }
            }
    }
}

private extension Array where Element == String {
    func deduplicated() -> [String] {
        var seen = Set<String>()
        return filter { seen.insert($0).inserted }
    }
}

public struct SDStructuredDocumentView: View {
    private let contentJSON: DocumentationContentJSON

    public init(contentJSON: DocumentationContentJSON) {
        self.contentJSON = contentJSON
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            ForEach(Array(contentJSON.blocks.enumerated()), id: \.offset) { _, block in
                blockView(block)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func blockView(_ block: DocumentationContentBlock) -> some View {
        switch block.type {
        case "heading":
            Text(block.text ?? "")
                .font(headingFont(level: block.level ?? 1))
                .foregroundStyle(SDColors.textPrimary)
                .padding(.top, headingTopPadding(level: block.level ?? 1))
        case "list":
            VStack(alignment: .leading, spacing: 4) {
                ForEach(block.items ?? [], id: \.self) { item in
                    HStack(alignment: .firstTextBaseline, spacing: 8) {
                        Text(block.style == "ordered" ? "1." : "•")
                            .foregroundStyle(SDColors.textMuted)
                        Text(item)
                            .foregroundStyle(SDColors.textPrimary)
                    }
                }
            }
            .font(.body)
        case "code":
            Text(block.code ?? block.text ?? "")
                .font(.system(.body, design: .monospaced))
                .foregroundStyle(SDColors.textPrimary)
                .textSelection(.enabled)
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(SDColors.elevatedSurface.opacity(0.72), in: .rect(cornerRadius: 8))
        case "blockquote":
            Text(block.text ?? "")
                .font(.body)
                .foregroundStyle(SDColors.textSecondary)
                .padding(.leading, 12)
                .overlay(alignment: .leading) {
                    Rectangle()
                        .fill(SDColors.border)
                        .frame(width: 3)
                }
        default:
            Text(block.text ?? "")
                .font(.body)
                .foregroundStyle(SDColors.textPrimary)
        }
    }

    private func headingFont(level: Int) -> Font {
        switch level {
        case 1: .largeTitle.weight(.bold)
        case 2: .title2.weight(.semibold)
        case 3: .title3.weight(.semibold)
        default: .headline
        }
    }

    private func headingTopPadding(level: Int) -> CGFloat {
        level == 1 ? 10 : 14
    }
}
