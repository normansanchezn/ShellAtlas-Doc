import SwiftUI
import SD_DesignSystem
import SD_Domain
import DS_Core

struct ExplorerView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = ExplorerViewModel()

    var body: some View {
        NavigationSplitView {
            DocsSidebar(viewModel: viewModel)
        } detail: {
            if viewModel.isEditingInPlace, let doc = viewModel.selectedDocument {
                LiveMarkdownEditor(
                    docTitle: doc.title,
                    initialContent: doc.content,
                    onSave: { content in viewModel.saveEdit(content: content) },
                    onCancel: { viewModel.cancelEdit() }
                )
            } else if let doc = viewModel.selectedDocument {
                DocumentReaderView(document: doc, onEdit: {
                    viewModel.isEditingInPlace = true
                })
            } else {
                ContentUnavailableView(
                    "Select a Document",
                    systemImage: "doc.text",
                    description: Text("Choose a document from the sidebar to read it.")
                )
            }
        }
        .navigationSplitViewStyle(.balanced)
        .task {
            guard let services else { return }
            try? await viewModel.load(services: services)
        }
        .sheet(isPresented: $viewModel.showNewDocDialog) {
            NewDocumentDialog(viewModel: viewModel)
        }
    }
}

// MARK: - Sidebar

private struct DocsSidebar: View {
    @Bindable var viewModel: ExplorerViewModel

    var body: some View {
        VStack(spacing: 0) {
            searchBar
            Divider()
            documentTree
        }
        .navigationTitle("Docs")
        .toolbar {
            ToolbarItem(placement: toolbarPlacement) {
                Button("New Document", systemImage: "square.and.pencil") {
                    viewModel.showNewDocDialog = true
                }
                .keyboardShortcut("n", modifiers: .command)
            }
            ToolbarItem(placement: toolbarPlacement) {
                Menu {
                    ForEach(DocumentGrouping.allCases, id: \.self) { mode in
                        Button(action: { viewModel.grouping = mode }) {
                            Label(mode.rawValue, systemImage: mode.systemImage)
                        }
                    }
                } label: {
                    Label("Group by \(viewModel.grouping.rawValue)", systemImage: "square.grid.3x1.folder.fill.badge.plus")
                }
                .labelStyle(.iconOnly)
            }
        }
    }

    private var searchBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(.secondary)
            TextField("Search docs…", text: $viewModel.searchText)
                .autocorrectionDisabled()
        }
        .padding(8)
        .background(.secondary.opacity(0.1), in: .rect(cornerRadius: 10))
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
    }

    private var documentTree: some View {
        List(selection: $viewModel.selectedDocumentID) {
            if viewModel.isLoading {
                ProgressView("Loading…")
                    .frame(maxWidth: .infinity)
            } else if viewModel.groupedSections.isEmpty {
                ContentUnavailableView.search
            } else {
                ForEach(viewModel.groupedSections, id: \.key) { section in
                    Section {
                        ForEach(section.documents) { doc in
                            DocsFileRow(document: doc)
                                .tag(doc.id)
                        }
                    } header: {
                        DocsFolderHeader(name: section.key, count: section.documents.count)
                    }
                }
            }
        }
        .listStyle(.sidebar)
    }
}

// MARK: - Folder Header

private struct DocsFolderHeader: View {
    let name: String
    let count: Int

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "folder.fill")
                .foregroundStyle(SDColors.shellYellow)
                .font(.caption)
            Text(name)
                .font(.caption.weight(.semibold))
                .foregroundStyle(.primary)
            Spacer()
            Text("\(count)")
                .font(.caption2.weight(.bold))
                .foregroundStyle(.secondary)
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
                .background(.secondary.opacity(0.15), in: .capsule)
        }
    }
}

// MARK: - File Row

private struct DocsFileRow: View {
    let document: KnowledgeDocument

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "doc.text.fill")
                .font(.callout)
                .foregroundStyle(statusColor)
            VStack(alignment: .leading, spacing: 2) {
                Text(document.title)
                    .font(.callout)
                    .lineLimit(2)
                HStack(spacing: 4) {
                    Text(document.platform.rawValue.capitalized)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    if !document.tags.isEmpty {
                        Text("·")
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                        Text(document.tags.prefix(2).joined(separator: ", "))
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .padding(.vertical, 2)
    }

    private var statusColor: Color {
        switch document.status {
        case .active:   .green
        case .review:   SDColors.shellOrange
        case .outdated: SDColors.shellRed
        case .archived: .secondary
        case .draft:    .secondary
        }
    }
}

// MARK: - Document Reader

struct DocumentReaderView: View {
    let document: KnowledgeDocument
    var onEdit: (() -> Void)? = nil
    @State private var showMetadata = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                documentHeader
                    .padding(.horizontal, 24)
                    .padding(.top, 20)
                    .padding(.bottom, 16)

                Divider()
                    .padding(.horizontal, 24)

                MarkdownBodyView(content: document.content)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 20)

                if showMetadata {
                    Divider()
                        .padding(.horizontal, 24)
                    metadataPanel
                        .padding(.horizontal, 24)
                        .padding(.vertical, 20)
                }
            }
        }
        .navigationTitle(document.title)
        .toolbar {
            ToolbarItem(placement: toolbarPlacement) {
                Button(action: { withAnimation(.spring(duration: 0.25)) { showMetadata.toggle() } }) {
                    Label("Info", systemImage: showMetadata ? "info.circle.fill" : "info.circle")
                        .foregroundStyle(showMetadata ? SDColors.shellYellow : .secondary)
                        .labelStyle(.iconOnly)
                }
            }
            if onEdit != nil {
                ToolbarItem(placement: toolbarPlacement) {
                    Button(action: { onEdit?() }) {
                        Label("Edit", systemImage: "pencil")
                    }
                    .tint(SDColors.shellOrange)
                }
            }
        }
    }

    // MARK: Header

    private var documentHeader: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                SDBadge(label: document.type.rawValue.capitalized, color: .blue, textColor: .gray)
                SDBadge(label: document.platform.rawValue.capitalized, color: .purple, textColor: .green)
                SDStatusChip(status: document.status)
                Spacer()
                SDScorePill(score: document.aiReviewPriority == .critical ? 35
                                  : document.confidence == .low ? 55 : 82)
            }

            Text(document.title)
                .font(.title2.weight(.bold))

            if !document.summary.isEmpty {
                Text(document.summary)
                    .font(.callout)
                    .foregroundStyle(.secondary)
            }

            HStack(spacing: 16) {
                if !document.owner.isEmpty {
                    Label(document.owner, systemImage: "person.fill")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                if !document.area.isEmpty {
                    Label(document.area, systemImage: "building.2.fill")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            if !document.tags.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(document.tags, id: \.self) { tag in
                            Text("#\(tag)")
                                .font(.caption2.weight(.medium))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(SDColors.shellYellow.opacity(0.15), in: .capsule)
                                .foregroundStyle(SDColors.shellYellow)
                        }
                    }
                }
            }
        }
    }

    // MARK: Metadata panel

    private var metadataPanel: some View {
        VStack(alignment: .leading, spacing: 16) {
            Label("Document Info", systemImage: "doc.badge.gearshape")
                .font(.headline)

            VStack(alignment: .leading, spacing: 8) {
                SDMetadataRow(label: "Last Validated", value: document.lastValidated.formatted(.dateTime.month().day().year()))
                SDMetadataRow(label: "Next Review", value: document.nextReview.formatted(.dateTime.month().day().year()))
                SDMetadataRow(label: "Review Frequency", value: document.reviewFrequency.rawValue)
                SDMetadataRow(label: "AI Priority", value: document.aiReviewPriority.rawValue.capitalized)
                SDMetadataRow(label: "Confidence", value: document.confidence.rawValue.capitalized)
                if !document.relatedTools.isEmpty {
                    SDMetadataRow(label: "Tools", value: document.relatedTools.joined(separator: ", "))
                }
                if !document.branches.isEmpty {
                    SDMetadataRow(label: "Branches", value: document.branches.joined(separator: ", "))
                }
                if !document.relatedRepositories.isEmpty {
                    SDMetadataRow(label: "Repositories", value: document.relatedRepositories.joined(separator: ", "))
                }
            }

            if !document.aiUpdateSignals.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Label("AI Update Signals", systemImage: "bolt.fill")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.shellOrange)
                    ForEach(document.aiUpdateSignals, id: \.self) { signal in
                        HStack(spacing: 6) {
                            Circle()
                                .fill(SDColors.shellOrange.opacity(0.6))
                                .frame(width: 4, height: 4)
                            Text(signal)
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(12)
                .background(SDColors.shellOrange.opacity(0.07), in: .rect(cornerRadius: 10))
            }

            if !document.openAIQuestions.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Label("Open Questions", systemImage: "questionmark.circle.fill")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(.blue)
                    ForEach(document.openAIQuestions, id: \.self) { q in
                        HStack(spacing: 6) {
                            Circle()
                                .fill(Color.blue.opacity(0.5))
                                .frame(width: 4, height: 4)
                            Text(q)
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(12)
                .background(Color.blue.opacity(0.06), in: .rect(cornerRadius: 10))
            }
        }
    }
}

private var toolbarPlacement: ToolbarItemPlacement {
#if os(macOS)
    .primaryAction
#else
    .topBarTrailing
#endif
}

// MARK: - Markdown Body Renderer

struct MarkdownBodyView: View {
    let content: String
    var suppressInitialHeading: String? = nil

    var body: some View {
        SDMarkdownBodyView(content: content, suppressInitialHeading: suppressInitialHeading)
    }

    private var parsedBlocks: [MarkdownBlock] {
        var blocks = MarkdownParser.parse(content)
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

// MARK: - Markdown Block Types

enum MarkdownBlock {
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

// MARK: - Markdown Block View

struct MarkdownBlockView: View {
    let block: MarkdownBlock

    var body: some View {
        switch block {
        case .h1(let text):
            Text(text)
                .font(.title.weight(.bold))
                .padding(.top, 16)
                .padding(.bottom, 4)
                .frame(maxWidth: .infinity, alignment: .leading)

        case .h2(let text):
            VStack(alignment: .leading, spacing: 4) {
                Text(text)
                    .font(.title3.weight(.semibold))
                Divider()
            }
            .padding(.top, 14)
            .padding(.bottom, 4)

        case .h3(let text):
            Text(text)
                .font(.headline)
                .padding(.top, 10)
                .padding(.bottom, 2)
                .frame(maxWidth: .infinity, alignment: .leading)

        case .paragraph(let text):
            inlineText(text)
                .font(.body)
                .textSelection(.enabled)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.vertical, 2)

        case .codeBlock(let lang, let code):
            VStack(alignment: .leading, spacing: 0) {
                if !lang.isEmpty {
                    Text(lang)
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(.secondary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(.secondary.opacity(0.12))
                }
                Text(code)
                    .font(.system(.caption, design: .monospaced))
                    .textSelection(.enabled)
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .background(.secondary.opacity(0.08), in: .rect(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(.secondary.opacity(0.2), lineWidth: 1)
            )
            .padding(.vertical, 4)

        case .checklistItem(let checked, let text):
            HStack(alignment: .top, spacing: 8) {
                Image(systemName: checked ? "checkmark.square.fill" : "square")
                    .font(.body)
                    .foregroundStyle(checked ? .green : .secondary)
                    .padding(.top, 1)
                inlineText(text)
                    .font(.body)
                    .foregroundStyle(checked ? .secondary : .primary)
                    .strikethrough(checked, color: .secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.vertical, 1)

        case .bulletItem(let indent, let text):
            HStack(alignment: .top, spacing: 8) {
                Text(indent == 0 ? "•" : "◦")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .padding(.top, 1)
                inlineText(text)
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.leading, CGFloat(indent) * 16)
            .padding(.vertical, 1)

        case .horizontalRule:
            Divider()
                .padding(.vertical, 8)

        case .tableRaw(let rows):
            MarkdownTableView(rows: rows)
                .padding(.vertical, 4)

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

// MARK: - Simple Markdown Table

struct MarkdownTableView: View {
    let rows: [String]

    var parsedRows: [[String]] {
        rows.compactMap { row -> [String]? in
            let trimmed = row.trimmingCharacters(in: .whitespaces)
            guard trimmed.hasPrefix("|") else { return nil }
            let cells = trimmed
                .split(separator: "|", omittingEmptySubsequences: false)
                .dropFirst()
                .dropLast()
                .map { $0.trimmingCharacters(in: .whitespaces) }
            guard !cells.isEmpty else { return nil }
            guard !cells.allSatisfy({ $0.allSatisfy({ $0 == "-" || $0 == ":" }) }) else { return nil }
            return cells
        }
    }

    var body: some View {
        if parsedRows.isEmpty { EmptyView() }
        else {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(parsedRows.enumerated()), id: \.offset) { index, cells in
                    HStack(spacing: 0) {
                        ForEach(Array(cells.enumerated()), id: \.offset) { _, cell in
                            Text(cell)
                                .font(index == 0 ? .caption.weight(.semibold) : .caption)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(
                                    index == 0 ? AnyShapeStyle(.secondary.opacity(0.15)) :
                                    index % 2 == 0 ? AnyShapeStyle(.secondary.opacity(0.05)) :
                                    AnyShapeStyle(.clear)
                                )
                        }
                    }
                    Divider()
                }
            }
            .background(.secondary.opacity(0.07), in: .rect(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(.secondary.opacity(0.2), lineWidth: 1)
            )
        }
    }
}

// MARK: - Markdown Parser

enum MarkdownParser {
    static func parse(_ raw: String) -> [MarkdownBlock] {
        let lines = raw.components(separatedBy: "\n")
        var blocks: [MarkdownBlock] = []
        var i = 0

        while i < lines.count {
            let line = lines[i]
            let trimmed = line.trimmingCharacters(in: .whitespaces)

            // Code block
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

            // Table: collect consecutive pipe rows
            if trimmed.hasPrefix("|") {
                var tableLines: [String] = []
                while i < lines.count && lines[i].trimmingCharacters(in: .whitespaces).hasPrefix("|") {
                    tableLines.append(lines[i])
                    i += 1
                }
                blocks.append(.tableRaw(tableLines))
                continue
            }

            // Headings
            if trimmed.hasPrefix("### ") {
                blocks.append(.h3(String(trimmed.dropFirst(4))))
            } else if trimmed.hasPrefix("## ") {
                blocks.append(.h2(String(trimmed.dropFirst(3))))
            } else if trimmed.hasPrefix("# ") {
                blocks.append(.h1(String(trimmed.dropFirst(2))))
            }
            // Horizontal rule
            else if trimmed == "---" || trimmed == "***" || trimmed == "___" {
                blocks.append(.horizontalRule)
            }
            // Checklist
            else if trimmed.hasPrefix("- [ ] ") {
                blocks.append(.checklistItem(checked: false, text: String(trimmed.dropFirst(6))))
            } else if trimmed.hasPrefix("- [x] ") || trimmed.hasPrefix("- [X] ") {
                blocks.append(.checklistItem(checked: true, text: String(trimmed.dropFirst(6))))
            }
            // Bullet
            else if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") || trimmed.hasPrefix("+ ") {
                let indent = line.prefix(while: { $0 == " " || $0 == "\t" }).count / 2
                blocks.append(.bulletItem(indent: indent, text: String(trimmed.dropFirst(2))))
            }
            // Empty line
            else if trimmed.isEmpty {
                blocks.append(.empty)
            }
            // Paragraph
            else {
                blocks.append(.paragraph(trimmed))
            }

            i += 1
        }

        return blocks
    }
}

// MARK: - New Document Dialog

struct NewDocumentDialog: View {
    @Bindable var viewModel: ExplorerViewModel
    @Environment(\.dismiss) private var dismiss
    @FocusState private var titleFocused: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section("Document") {
                    TextField("Title", text: $viewModel.newDocTitle)
                        .focused($titleFocused)
                        .autocorrectionDisabled()
                }
                Section("Folder") {
                    Picker("Type", selection: $viewModel.newDocType) {
                        ForEach(DocumentType.allCases, id: \.self) { type in
                            Label(type.displayName, systemImage: "folder.fill")
                                .tag(type)
                        }
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }
            }
            .navigationTitle("New Document")
#if os(iOS)
            .navigationBarTitleDisplayMode(.inline)
#endif
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Create") {
                        viewModel.startCreating(
                            title: viewModel.newDocTitle,
                            type: viewModel.newDocType
                        )
                    }
                    .disabled(viewModel.newDocTitle.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .onAppear { titleFocused = true }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    ExplorerView()
}
