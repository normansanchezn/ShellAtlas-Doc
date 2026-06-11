import SwiftUI
import SD_DesignSystem

struct MarkdownLiveEditor: View {
    @Binding private var content: String
    @State private var blocks: [LiveMarkdownBlock] = []
    @State private var lastSyncedContent = ""
    @State private var activeBlockID: UUID?
    @FocusState private var focusedBlockID: UUID?

    init(content: Binding<String>) {
        self._content = content
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Markdown")
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                Spacer()
                Label("Live Preview", systemImage: "text.cursor")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(SDColors.textMuted)
            }

            LazyVStack(alignment: .leading, spacing: 4) {
                ForEach($blocks) { $block in
                    LiveMarkdownBlockView(
                        block: $block,
                        isActive: activeBlockID == block.id,
                        activate: {
                            activate(block.id)
                        },
                        changed: {
                            updateBlockType(block.id)
                            syncContent()
                        }
                    )
                    .focused($focusedBlockID, equals: block.id)
                    .id(block.id)
                }

                Color.clear
                    .frame(height: 160)
                    .contentShape(.rect)
                    .onTapGesture {
                        focusLastEditableBlock()
                    }
            }
            .padding(.horizontal, 16)
            .padding(.top, 14)
            .padding(.bottom, 0)
            .frame(minHeight: 540, alignment: .topLeading)
            .frame(maxWidth: .infinity, alignment: .topLeading)
            .background(SDColors.cardBackground.opacity(0.64), in: .rect(cornerRadius: 8))
            .overlay {
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(SDColors.border.opacity(0.72), lineWidth: 1)
            }
        }
        .onAppear {
            guard blocks.isEmpty else { return }
            resetBlocks(from: content, focusFirstBlock: false)
        }
        .onChange(of: content) { _, newValue in
            guard newValue != lastSyncedContent, activeBlockID == nil else { return }
            resetBlocks(from: newValue, focusFirstBlock: false)
        }
        .onChange(of: focusedBlockID) { _, newValue in
            activeBlockID = newValue
        }
    }

    private func activate(_ id: UUID) {
        activeBlockID = id
        focusedBlockID = id
    }

    private func focusLastEditableBlock() {
        if blocks.isEmpty {
            blocks = [LiveMarkdownBlock(rawMarkdown: "")]
        } else if blocks.last?.rawMarkdown.isEmpty == false {
            blocks.append(LiveMarkdownBlock(rawMarkdown: ""))
            syncContent()
        }

        if let id = blocks.last?.id {
            activate(id)
        }
    }

    private func updateBlockType(_ id: UUID) {
        guard let index = blocks.firstIndex(where: { $0.id == id }) else { return }
        blocks[index].type = LiveMarkdownParser.blockType(for: blocks[index].rawMarkdown)
    }

    private func syncContent() {
        let joined = blocks.map(\.rawMarkdown).joined(separator: "\n")
        lastSyncedContent = joined
        content = joined
    }

    private func resetBlocks(from raw: String, focusFirstBlock: Bool) {
        let parsed = LiveMarkdownParser.parse(raw)
        blocks = parsed.isEmpty ? [LiveMarkdownBlock(rawMarkdown: "")] : parsed
        lastSyncedContent = blocks.map(\.rawMarkdown).joined(separator: "\n")

        if focusFirstBlock, let id = blocks.first?.id {
            Task { @MainActor in
                activeBlockID = id
                focusedBlockID = id
            }
        }
    }
}

private struct LiveMarkdownBlock: Identifiable, Equatable {
    let id: UUID
    var type: LiveMarkdownBlockType
    var rawMarkdown: String

    init(id: UUID = UUID(), rawMarkdown: String) {
        self.id = id
        self.rawMarkdown = rawMarkdown
        self.type = LiveMarkdownParser.blockType(for: rawMarkdown)
    }
}

private enum LiveMarkdownBlockType: Equatable {
    case paragraph
    case heading(level: Int)
    case unorderedList
    case orderedList
    case taskList
    case blockquote
    case fencedCodeBlock(language: String?)
    case table
    case horizontalRule
    case empty
    case unknown
}

private struct LiveMarkdownBlockView: View {
    @Binding var block: LiveMarkdownBlock
    let isActive: Bool
    let activate: () -> Void
    let changed: () -> Void

    var body: some View {
        Group {
            if isActive {
                activeEditor
            } else {
                inactiveRenderer
                    .contentShape(.rect)
                    .onTapGesture(perform: activate)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var activeEditor: some View {
        TextEditor(text: Binding(
            get: { block.rawMarkdown },
            set: { newValue in
                block.rawMarkdown = newValue
                changed()
            }
        ))
        .font(activeFont)
        .foregroundStyle(SDColors.textPrimary)
        .lineSpacing(2)
        .scrollContentBackground(.hidden)
        .textEditorStyle(.plain)
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .frame(minHeight: activeEditorHeight, maxHeight: activeEditorHeight)
        .background(activeBackground, in: .rect(cornerRadius: 7))
        .overlay {
            RoundedRectangle(cornerRadius: 7)
                .strokeBorder(SDColors.secondary.opacity(0.34), lineWidth: 1)
        }
    }

    @ViewBuilder
    private var inactiveRenderer: some View {
        switch block.type {
        case .heading(let level):
            Text(block.rawMarkdown.dropMarkdownHeadingPrefix)
                .font(headingFont(level))
                .foregroundStyle(SDColors.textPrimary)
                .padding(.top, level == 1 ? 10 : 7)
                .padding(.bottom, 3)

        case .paragraph:
            inlineText(block.rawMarkdown)
                .font(.body)
                .foregroundStyle(SDColors.textPrimary)
                .lineSpacing(2)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.vertical, 2)

        case .unorderedList:
            listLines(block.rawMarkdown, markerStyle: .bullet)

        case .orderedList:
            listLines(block.rawMarkdown, markerStyle: .numbered)

        case .taskList:
            taskListLines(block.rawMarkdown)

        case .blockquote:
            VStack(alignment: .leading, spacing: 4) {
                ForEach(block.rawMarkdown.nonEmptyLines, id: \.self) { line in
                    inlineText(line.dropQuotePrefix)
                        .font(.body)
                        .foregroundStyle(SDColors.textSecondary)
                }
            }
            .padding(.leading, 12)
            .padding(.vertical, 6)
            .frame(maxWidth: .infinity, alignment: .leading)
            .overlay(alignment: .leading) {
                Rectangle()
                    .fill(SDColors.secondary.opacity(0.55))
                    .frame(width: 3)
            }

        case .fencedCodeBlock(let language):
            codeBlock(language: language, raw: block.rawMarkdown)

        case .table:
            LiveMarkdownTableView(rawMarkdown: block.rawMarkdown)
                .padding(.vertical, 4)

        case .horizontalRule:
            Rectangle()
                .fill(SDColors.border.opacity(0.78))
                .frame(height: 1)
                .padding(.vertical, 8)

        case .empty:
            Color.clear
                .frame(height: 20)
                .contentShape(.rect)

        case .unknown:
            Text(block.rawMarkdown)
                .font(.body)
                .foregroundStyle(SDColors.textPrimary)
                .padding(.vertical, 2)
        }
    }

    private var activeEditorHeight: CGFloat {
        let lineCount = max(1, block.rawMarkdown.components(separatedBy: "\n").count)
        let lineHeight: CGFloat = {
            switch block.type {
            case .heading(let level): level == 1 ? 34 : level == 2 ? 28 : 24
            case .fencedCodeBlock: 21
            default: 23
            }
        }()
        return max(48, CGFloat(lineCount) * lineHeight + 24)
    }

    private var activeFont: Font {
        switch block.type {
        case .heading(let level):
            headingFont(level)
        case .fencedCodeBlock:
            .system(.body, design: .monospaced)
        default:
            .body
        }
    }

    private var activeBackground: Color {
        switch block.type {
        case .fencedCodeBlock:
            SDColors.background.opacity(0.72)
        default:
            SDColors.elevatedSurface.opacity(0.58)
        }
    }

    private func headingFont(_ level: Int) -> Font {
        switch level {
        case 1: .title.weight(.bold)
        case 2: .title3.weight(.semibold)
        default: .headline.weight(.semibold)
        }
    }

    @ViewBuilder
    private func listLines(_ raw: String, markerStyle: ListMarkerStyle) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            ForEach(Array(raw.nonEmptyLines.enumerated()), id: \.offset) { _, line in
                HStack(alignment: .top, spacing: 8) {
                    Text(markerStyle.marker(for: line))
                        .font(.body)
                        .foregroundStyle(SDColors.textMuted)
                        .frame(minWidth: markerStyle == .numbered ? 28 : 10, alignment: .trailing)
                    inlineText(markerStyle.text(for: line))
                        .font(.body)
                        .foregroundStyle(SDColors.textPrimary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
        .padding(.vertical, 2)
    }

    @ViewBuilder
    private func taskListLines(_ raw: String) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            ForEach(Array(raw.nonEmptyLines.enumerated()), id: \.offset) { _, line in
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: line.isCheckedTaskLine ? "checkmark.square.fill" : "square")
                        .font(.body)
                        .foregroundStyle(line.isCheckedTaskLine ? SDColors.success : SDColors.textMuted)
                        .padding(.top, 1)
                    inlineText(line.dropTaskPrefix)
                        .font(.body)
                        .foregroundStyle(line.isCheckedTaskLine ? SDColors.textSecondary : SDColors.textPrimary)
                        .strikethrough(line.isCheckedTaskLine, color: SDColors.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
        .padding(.vertical, 2)
    }

    @ViewBuilder
    private func codeBlock(language: String?, raw: String) -> some View {
        let code = raw.fencedCodeBody
        VStack(alignment: .leading, spacing: 0) {
            if let language, !language.isEmpty {
                Text(language.uppercased())
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(SDColors.secondary)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(SDColors.secondary.opacity(0.08))
            }

            ScrollView(.horizontal, showsIndicators: true) {
                Text(code)
                    .font(.system(.callout, design: .monospaced))
                    .foregroundStyle(SDColors.textPrimary)
                    .textSelection(.enabled)
                    .padding(10)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .background(SDColors.background.opacity(0.84), in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.9), lineWidth: 1)
        }
        .padding(.vertical, 4)
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

    private enum ListMarkerStyle {
        case bullet
        case numbered

        func marker(for line: String) -> String {
            switch self {
            case .bullet:
                "•"
            case .numbered:
                line.numberedPrefix
            }
        }

        func text(for line: String) -> String {
            switch self {
            case .bullet:
                line.dropListPrefix
            case .numbered:
                line.dropNumberedPrefix
            }
        }
    }
}

private struct LiveMarkdownTableView: View {
    let rawMarkdown: String

    private var table: [[String]] {
        LiveMarkdownParser.tableRows(from: rawMarkdown).filter { !$0.isSeparatorRow }
    }

    var body: some View {
        if table.isEmpty {
            EmptyView()
        } else {
            ScrollView(.horizontal, showsIndicators: table.columnCount > 4) {
                VStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(table.enumerated()), id: \.offset) { rowIndex, row in
                        HStack(spacing: 0) {
                            ForEach(0..<table.columnCount, id: \.self) { column in
                                Text(row[safe: column] ?? "")
                                    .font(rowIndex == 0 ? .caption.weight(.semibold) : .caption)
                                    .foregroundStyle(SDColors.textPrimary)
                                    .lineLimit(nil)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 7)
                                    .frame(minWidth: 120, alignment: .leading)
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
                .background(SDColors.background.opacity(0.42), in: .rect(cornerRadius: 8))
                .overlay {
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(SDColors.border.opacity(0.82), lineWidth: 1)
                }
            }
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
}

private enum LiveMarkdownParser {
    static func parse(_ raw: String) -> [LiveMarkdownBlock] {
        let lines = raw.components(separatedBy: "\n")
        var blocks: [LiveMarkdownBlock] = []
        var index = 0

        while index < lines.count {
            let line = lines[index]
            let trimmed = line.trimmingCharacters(in: .whitespaces)

            if trimmed.hasPrefix("```") {
                let start = index
                index += 1
                while index < lines.count {
                    let candidate = lines[index].trimmingCharacters(in: .whitespaces)
                    if candidate.hasPrefix("```") {
                        index += 1
                        break
                    }
                    index += 1
                }
                blocks.append(block(from: lines[start..<index]))
                continue
            }

            if isValidTableStart(lines, at: index) {
                let start = index
                index += 2
                while index < lines.count, tableRows(from: lines[index]).isEmpty == false {
                    index += 1
                }
                blocks.append(block(from: lines[start..<index]))
                continue
            }

            if isListLine(trimmed) {
                let start = index
                index += 1
                while index < lines.count, isListLine(lines[index].trimmingCharacters(in: .whitespaces)) {
                    index += 1
                }
                blocks.append(block(from: lines[start..<index]))
                continue
            }

            if trimmed.hasPrefix(">") {
                let start = index
                index += 1
                while index < lines.count, lines[index].trimmingCharacters(in: .whitespaces).hasPrefix(">") {
                    index += 1
                }
                blocks.append(block(from: lines[start..<index]))
                continue
            }

            if trimmed.isEmpty || isHeading(trimmed) || isHorizontalRule(trimmed) {
                blocks.append(LiveMarkdownBlock(rawMarkdown: line))
                index += 1
                continue
            }

            let start = index
            index += 1
            while index < lines.count {
                let next = lines[index].trimmingCharacters(in: .whitespaces)
                if next.isEmpty || next.hasPrefix("```") || isValidTableStart(lines, at: index) || isListLine(next) || next.hasPrefix(">") || isHeading(next) || isHorizontalRule(next) {
                    break
                }
                index += 1
            }
            blocks.append(block(from: lines[start..<index]))
        }

        return blocks
    }

    static func blockType(for raw: String) -> LiveMarkdownBlockType {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return .empty }

        if trimmed.hasPrefix("```") {
            let firstLine = trimmed.components(separatedBy: "\n").first ?? ""
            let language = String(firstLine.dropFirst(3)).trimmingCharacters(in: .whitespacesAndNewlines)
            return .fencedCodeBlock(language: language.isEmpty ? nil : language)
        }

        if isValidTable(raw) { return .table }
        if trimmed == "---" || trimmed == "***" || trimmed == "___" { return .horizontalRule }
        if trimmed.hasPrefix("### ") { return .heading(level: 3) }
        if trimmed.hasPrefix("## ") { return .heading(level: 2) }
        if trimmed.hasPrefix("# ") { return .heading(level: 1) }
        if trimmed.hasPrefix(">") { return .blockquote }
        if trimmed.components(separatedBy: "\n").allSatisfy({ $0.trimmingCharacters(in: .whitespaces).isTaskLine }) { return .taskList }
        if trimmed.components(separatedBy: "\n").allSatisfy({ $0.trimmingCharacters(in: .whitespaces).isOrderedListLine }) { return .orderedList }
        if trimmed.components(separatedBy: "\n").allSatisfy({ $0.trimmingCharacters(in: .whitespaces).isUnorderedListLine }) { return .unorderedList }
        return .paragraph
    }

    static func tableRows(from raw: String) -> [[String]] {
        raw.components(separatedBy: "\n").compactMap(tableRow)
    }

    private static func block(from lines: ArraySlice<String>) -> LiveMarkdownBlock {
        LiveMarkdownBlock(rawMarkdown: lines.joined(separator: "\n"))
    }

    private static func isValidTableStart(_ lines: [String], at index: Int) -> Bool {
        guard index + 1 < lines.count else { return false }
        let candidate = [lines[index], lines[index + 1]].joined(separator: "\n")
        return isValidTable(candidate)
    }

    private static func isValidTable(_ raw: String) -> Bool {
        let rows = tableRows(from: raw)
        guard rows.count >= 2, rows[1].isSeparatorRow else { return false }
        return rows[0].count == rows[1].count && rows[0].count > 1
    }

    private static func tableRow(_ line: String) -> [String]? {
        let trimmed = line.trimmingCharacters(in: .whitespaces)
        guard trimmed.hasPrefix("|"), trimmed.hasSuffix("|") else { return nil }

        let cells = trimmed
            .split(separator: "|", omittingEmptySubsequences: false)
            .dropFirst()
            .dropLast()
            .map { $0.trimmingCharacters(in: .whitespaces) }

        return cells.isEmpty ? nil : cells
    }

    private static func isHeading(_ line: String) -> Bool {
        line.hasPrefix("# ") || line.hasPrefix("## ") || line.hasPrefix("### ")
    }

    private static func isHorizontalRule(_ line: String) -> Bool {
        line == "---" || line == "***" || line == "___"
    }

    private static func isListLine(_ line: String) -> Bool {
        line.isTaskLine || line.isOrderedListLine || line.isUnorderedListLine
    }
}

private extension String {
    var nonEmptyLines: [String] {
        components(separatedBy: "\n").filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }
    }

    var dropMarkdownHeadingPrefix: String {
        trimmingCharacters(in: .whitespaces).replacingOccurrences(of: #"^#{1,3}\s+"#, with: "", options: .regularExpression)
    }

    var dropListPrefix: String {
        trimmingCharacters(in: .whitespaces).replacingOccurrences(of: #"^[-*+]\s+"#, with: "", options: .regularExpression)
    }

    var dropNumberedPrefix: String {
        trimmingCharacters(in: .whitespaces).replacingOccurrences(of: #"^\d+\.\s+"#, with: "", options: .regularExpression)
    }

    var numberedPrefix: String {
        let trimmed = trimmingCharacters(in: .whitespaces)
        guard let range = trimmed.range(of: #"^\d+\."#, options: .regularExpression) else { return "1." }
        return String(trimmed[range])
    }

    var dropQuotePrefix: String {
        trimmingCharacters(in: .whitespaces).replacingOccurrences(of: #"^>\s?"#, with: "", options: .regularExpression)
    }

    var isUnorderedListLine: Bool {
        range(of: #"^[-*+]\s+(?!\[[ xX]\]\s).+"#, options: .regularExpression) != nil
    }

    var isOrderedListLine: Bool {
        range(of: #"^\d+\.\s+.+$"#, options: .regularExpression) != nil
    }

    var isTaskLine: Bool {
        range(of: #"^[-*+]\s+\[[ xX]\]\s+.*$"#, options: .regularExpression) != nil
    }

    var isCheckedTaskLine: Bool {
        range(of: #"^[-*+]\s+\[[xX]\]\s+.*$"#, options: .regularExpression) != nil
    }

    var dropTaskPrefix: String {
        trimmingCharacters(in: .whitespaces).replacingOccurrences(of: #"^[-*+]\s+\[[ xX]\]\s*"#, with: "", options: .regularExpression)
    }

    var fencedCodeBody: String {
        var lines = components(separatedBy: "\n")
        if lines.first?.trimmingCharacters(in: .whitespaces).hasPrefix("```") == true {
            lines.removeFirst()
        }
        if lines.last?.trimmingCharacters(in: .whitespaces).hasPrefix("```") == true {
            lines.removeLast()
        }
        return lines.joined(separator: "\n")
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

private extension Array where Element == [String] {
    var columnCount: Int {
        map(\.count).max() ?? 0
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
