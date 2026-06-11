import SwiftUI
import SD_Domain
import DS_Core

// MARK: - Editor Line Model

struct EditorLine: Identifiable {
    var id = UUID()
    var text: String
}

// MARK: - Live Markdown Editor

struct LiveMarkdownEditor: View {
    let docTitle: String
    let initialContent: String
    var onSave: (String) -> Void
    var onCancel: () -> Void

    @State private var lines: [EditorLine]
    @FocusState private var focusedID: UUID?

    init(docTitle: String, initialContent: String, onSave: @escaping (String) -> Void, onCancel: @escaping () -> Void) {
        self.docTitle = docTitle
        self.initialContent = initialContent
        self.onSave = onSave
        self.onCancel = onCancel
        let rawLines = initialContent.isEmpty ? [""] : initialContent.components(separatedBy: "\n")
        _lines = State(initialValue: rawLines.map { EditorLine(text: $0) })
    }

    private var content: String {
        lines.map(\.text).joined(separator: "\n")
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                ForEach($lines) { $line in
                    let id = $line.wrappedValue.id
                    EditorLineCell(
                        text: $line.text,
                        isFocused: focusedID == id,
                        onTap: { focusedID = id },
                        onSubmit: { insertLineAfter(id: id) },
                        onDeleteIfEmpty: { deleteLineIfEmpty(id: id) }
                    )
                    .focused($focusedID, equals: id)
                }
                // Tap below last line to focus it
                Color.clear
                    .frame(maxWidth: .infinity, minHeight: 200)
                    .contentShape(.rect)
                    .onTapGesture { focusedID = lines.last?.id }
            }
            .padding(.horizontal, 24)
            .padding(.top, 20)
            .padding(.bottom, 40)
        }
        .navigationTitle(docTitle)
#if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
#endif
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel", role: .cancel) { onCancel() }
            }
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") { onSave(content) }
                    .fontWeight(.semibold)
            }
        }
        .task {
            if focusedID == nil {
                focusedID = lines.first?.id
            }
        }
    }

    // MARK: - Line Mutations

    private func insertLineAfter(id: UUID) {
        guard let index = lines.firstIndex(where: { $0.id == id }) else { return }
        let newLine = EditorLine(text: "")
        let newID = newLine.id
        lines.insert(newLine, at: index + 1)
        Task { @MainActor in
            focusedID = newID
        }
    }

    private func deleteLineIfEmpty(id: UUID) {
        guard lines.count > 1,
              let index = lines.firstIndex(where: { $0.id == id }),
              lines[index].text.isEmpty else { return }
        lines.remove(at: index)
        focusedID = lines[max(0, index - 1)].id
    }

}

// MARK: - Editor Line Cell

private struct EditorLineCell: View {
    @Binding var text: String
    let isFocused: Bool
    let onTap: () -> Void
    let onSubmit: () -> Void
    let onDeleteIfEmpty: () -> Void

    var body: some View {
        Group {
            if isFocused {
                rawEditor
            } else {
                renderedLine
                    .contentShape(.rect)
                    .onTapGesture(perform: onTap)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, lineVerticalPadding)
    }

    // MARK: Raw Editor (focused)

    private var rawEditor: some View {
        TextField("", text: $text, axis: .vertical)
            .font(editingFont)
            .foregroundStyle(editingColor)
            .textFieldStyle(.plain)
            .onSubmit(onSubmit)
            .onChange(of: text) { _, newValue in
                if newValue.isEmpty { onDeleteIfEmpty() }
            }
    }

    // MARK: Rendered Preview (unfocused)

    @ViewBuilder
    private var renderedLine: some View {
        if text.isEmpty {
            Color.clear.frame(height: 8)
        } else {
            let block = MarkdownParser.parseLine(text)
            MarkdownBlockView(block: block)
        }
    }

    // MARK: - Font & Style

    private var editingFont: Font {
        let t = text
        if t.hasPrefix("# ")   { return .title.weight(.bold) }
        if t.hasPrefix("## ")  { return .title3.weight(.semibold) }
        if t.hasPrefix("### ") { return .headline }
        if t.hasPrefix("```")  { return .system(.body, design: .monospaced) }
        if t.hasPrefix("    ") { return .system(.body, design: .monospaced) }
        return .body
    }

    private var editingColor: Color {
        // Markdown syntax shown in secondary so content reads clearly
        let t = text
        if t.hasPrefix("#") || t.hasPrefix("-") || t.hasPrefix(">") || t.hasPrefix("```") {
            return .primary
        }
        return .primary
    }

    private var lineVerticalPadding: CGFloat {
        let t = text
        if t.hasPrefix("# ")   { return 6 }
        if t.hasPrefix("## ")  { return 4 }
        if t.hasPrefix("### ") { return 3 }
        return 2
    }
}

// MARK: - MarkdownParser single-line extension

extension MarkdownParser {
    static func parseLine(_ line: String) -> MarkdownBlock {
        let trimmed = line.trimmingCharacters(in: .whitespaces)
        if trimmed.hasPrefix("### ") { return .h3(String(trimmed.dropFirst(4))) }
        if trimmed.hasPrefix("## ")  { return .h2(String(trimmed.dropFirst(3))) }
        if trimmed.hasPrefix("# ")   { return .h1(String(trimmed.dropFirst(2))) }
        if trimmed == "---" || trimmed == "***" || trimmed == "___" { return .horizontalRule }
        if trimmed.hasPrefix("- [ ] ") { return .checklistItem(checked: false, text: String(trimmed.dropFirst(6))) }
        if trimmed.hasPrefix("- [x] ") || trimmed.hasPrefix("- [X] ") {
            return .checklistItem(checked: true, text: String(trimmed.dropFirst(6)))
        }
        if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") || trimmed.hasPrefix("+ ") {
            let indent = line.prefix(while: { $0 == " " || $0 == "\t" }).count / 2
            return .bulletItem(indent: indent, text: String(trimmed.dropFirst(2)))
        }
        if trimmed.isEmpty { return .empty }
        return .paragraph(trimmed)
    }
}
