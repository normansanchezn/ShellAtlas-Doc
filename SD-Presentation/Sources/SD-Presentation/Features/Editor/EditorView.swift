import SwiftUI
import SD_DesignSystem
import SD_Domain
import DS_Core

struct EditorView: View {
    @Environment(\.appServices) private var services
    @Environment(\.dismiss) private var dismiss
    let documentID: String

    @State private var viewModel = EditorViewModel()
    @FocusState private var editorFocused: Bool

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading document…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.draft != nil {
                editorContent
            } else {
                ContentUnavailableView("Document not found", systemImage: "doc.badge.exclamationmark")
            }
        }
        .navigationTitle(viewModel.draft?.title ?? "Editor")
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Close") { dismiss() }
            }
            ToolbarItemGroup(placement: .confirmationAction) {
                if viewModel.isSaving {
                    ProgressView().scaleEffect(0.7)
                } else {
                    Button("Save") {
                        Task { await viewModel.save() }
                    }
                    .disabled(!viewModel.isDirty)
                    .foregroundStyle(viewModel.isDirty ? SDColors.shellOrange : .secondary)
                }
            }
            ToolbarItem(placement: .automatic) {
                Button(action: { viewModel.isPreviewMode.toggle() }) {
                    Label(
                        viewModel.isPreviewMode ? "Edit" : "Preview",
                        systemImage: viewModel.isPreviewMode ? "pencil" : "eye"
                    )
                }
            }
        }
        .task {
            guard let services else { return }
        await viewModel.load(documentID: documentID, services: services)
        }
        .onDisappear {
            viewModel.cancelAutoSave()
        }
    }

    private var editorContent: some View {
        VStack(spacing: 0) {
            statusBar
            Divider()
            if viewModel.isPreviewMode {
                previewPane
            } else {
                editPane
            }
        }
    }

    private var statusBar: some View {
        HStack(spacing: 12) {
            if let saved = viewModel.lastSaved {
                Label("Saved \(saved.formatted(.relative(presentation: .named)))", systemImage: "checkmark.circle.fill")
                    .font(.caption)
                    .foregroundStyle(.green)
            } else if viewModel.isDirty {
                Label("Unsaved changes", systemImage: "circle.fill")
                    .font(.caption)
                    .foregroundStyle(SDColors.shellOrange)
            } else {
                Label("No changes", systemImage: "checkmark")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text(viewModel.isPreviewMode ? "Preview" : "Edit")
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 6)
        .background(.secondary.opacity(0.07))
    }

    private var editPane: some View {
        TextEditor(text: Binding(
            get: { viewModel.draft?.content ?? "" },
            set: { viewModel.draft?.content = $0 }
        ))
        .font(.system(.body, design: .monospaced))
        .focused($editorFocused)
        .padding(16)
        .onChange(of: viewModel.draft?.content) {
            viewModel.scheduleAutoSave()
        }
    }

    private var previewPane: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(viewModel.draft?.title ?? "")
                    .font(.title2.weight(.bold))
                let content = viewModel.draft?.content ?? ""
                if let attributed = try? AttributedString(
                    markdown: content,
                    options: .init(interpretedSyntax: .inlineOnlyPreservingWhitespace)
                ) {
                    Text(attributed)
                        .font(.body)
                        .textSelection(.enabled)
                        .frame(maxWidth: .infinity, alignment: .leading)
                } else {
                    Text(content)
                        .font(.body)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .padding(20)
        }
    }
}

#Preview {
    NavigationStack {
        EditorView(documentID: "doc-001")
    }
}
