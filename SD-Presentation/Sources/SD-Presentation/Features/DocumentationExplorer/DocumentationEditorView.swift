import SwiftUI
import SD_DesignSystem
import SD_Domain

struct DocumentationEditorView: View {
    @Environment(\.appServices) private var services
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: DocumentationEditorViewModel
    @State private var showsLeaveConfirmation = false
    private let onSaved: (String) -> Void

    init(mode: DocumentationEditorMode, onSaved: @escaping (String) -> Void) {
        self._viewModel = State(initialValue: DocumentationEditorViewModel(mode: mode))
        self.onSaved = onSaved
    }

    var body: some View {
        @Bindable var viewModel = viewModel

        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                editorTopBar

                if viewModel.isLoadingDocument {
                    SDLottieLoadingView("Opening document...", message: "Loading content and attributes for editing.")
                } else {
                    editorBody
                }
            }

            if !viewModel.isLoadingDocument {
                floatingSaveButton
                    .padding(.trailing, 28)
                    .padding(.bottom, 24)
            }

            if viewModel.isSaving {
                savingOverlay
            }
        }
        .navigationBarBackButtonHidden()
        .confirmationDialog(
            "You have unsaved changes. Do you want to save before leaving?",
            isPresented: $showsLeaveConfirmation,
            titleVisibility: .visible
        ) {
            Button("Save and Leave") {
                Task {
                    await saveAndClose()
                }
            }
            .disabled(!viewModel.canSave)

            Button("Leave Without Saving", role: .destructive) {
                dismiss()
            }

            Button("Cancel", role: .cancel) {}
        }
        .task {
            guard let services else { return }
            viewModel.configure(services: services)
            await viewModel.onAppear()
        }
        .task(id: viewModel.content) {
            try? await Task.sleep(for: .milliseconds(1200))
            guard !Task.isCancelled else { return }
            await viewModel.autosaveDraftIfPossible()
        }
    }

    private var editorBody: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                DocumentIdentityFields(viewModel: viewModel)
                DocumentAttributeEditor(viewModel: viewModel)
                MarkdownLiveEditor(content: $viewModel.content)
                VersionHistoryPanel(viewModel: viewModel)
                validationPanel
            }
            .padding(.horizontal, 34)
            .padding(.vertical, 28)
            .padding(.bottom, 92)
            .frame(maxWidth: 920, alignment: .topLeading)
            .frame(maxWidth: .infinity, alignment: .top)
        }
        .background(SDColors.contentBackground)
    }

    private var editorTopBar: some View {
        HStack(spacing: 12) {
            SDGlassIconButton(systemImage: "chevron.left", accessibilityLabel: "Back") {
                if viewModel.hasUnsavedChanges {
                    showsLeaveConfirmation = true
                } else {
                    dismiss()
                }
            }

            Spacer()

            if viewModel.effectiveSaveState != .idle {
                Label(viewModel.effectiveSaveState.displayName, systemImage: saveStateIcon)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(saveStateTint)
            }

            if let draftMessage = viewModel.draftMessage {
                Text(draftMessage)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(draftMessage == "Draft save failed" ? SDColors.danger : SDColors.textMuted)
            }
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 12)
        .background(SDColors.topBarBackground)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(SDColors.border.opacity(0.72))
                .frame(height: 1)
        }
    }

    private var floatingSaveButton: some View {
        SDPrimaryButton(viewModel.primarySaveButtonTitle, systemImage: "checkmark", isDisabled: !viewModel.canSave) {
            Task {
                await saveAndClose()
            }
        }
        .shadow(color: SDColors.background.opacity(0.28), radius: 16, x: 0, y: 8)
        .accessibilityHint("Saves the document when content or attributes have unsaved changes.")
    }

    private func saveAndClose() async {
        if let documentID = await viewModel.save() {
            onSaved(documentID)
            dismiss()
        }
    }

    private var saveStateIcon: String {
        switch viewModel.effectiveSaveState {
        case .idle: "checkmark.circle"
        case .dirty: "circle.fill"
        case .saving: "arrow.triangle.2.circlepath"
        case .saved: "checkmark.circle.fill"
        case .failed: "exclamationmark.triangle.fill"
        }
    }

    private var saveStateTint: Color {
        switch viewModel.effectiveSaveState {
        case .idle: SDColors.textMuted
        case .dirty: SDColors.warning
        case .saving: SDColors.secondary
        case .saved: SDColors.success
        case .failed: SDColors.danger
        }
    }

    private var savingOverlay: some View {
        ZStack {
            SDColors.background.opacity(0.18)
                .ignoresSafeArea()

            SDLottieLoadingView("Saving changes...", message: "Applying document content and attribute updates.", size: CGSize(width: 112, height: 88))
                .frame(width: 360, height: 240)
                .background(.regularMaterial, in: .rect(cornerRadius: 14))
                .overlay {
                    RoundedRectangle(cornerRadius: 14)
                        .strokeBorder(SDColors.border.opacity(0.72), lineWidth: 1)
                }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
            .transition(.opacity.combined(with: .scale(scale: 0.98)))
    }

    @ViewBuilder
    private var validationPanel: some View {
        if !viewModel.validationMessages.isEmpty {
            VStack(alignment: .leading, spacing: 7) {
                Text("Required before saving")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(SDColors.textMuted)
                    .textCase(.uppercase)
                ForEach(viewModel.validationMessages, id: \.self) { message in
                    Label(message, systemImage: "exclamationmark.circle")
                        .font(.caption)
                        .foregroundStyle(SDColors.danger)
                }
            }
            .padding(12)
            .background(SDColors.dangerSoft, in: .rect(cornerRadius: 8))
        }

        if let errorMessage = viewModel.errorMessage {
            Label(errorMessage, systemImage: "exclamationmark.triangle")
                .font(.callout)
                .foregroundStyle(SDColors.danger)
        }
    }
}

private struct VersionHistoryPanel: View {
    @Bindable var viewModel: DocumentationEditorViewModel

    var body: some View {
        if case .edit = viewModel.mode {
            SDMetadataPanel("Version History") {
                if viewModel.isLoadingVersions {
                    ProgressView("Loading versions...")
                } else if viewModel.versions.isEmpty {
                    Text("No published versions yet.")
                        .font(.callout)
                        .foregroundStyle(SDColors.textMuted)
                } else {
                    VStack(spacing: 8) {
                        ForEach(viewModel.versions) { version in
                            HStack(spacing: 10) {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Version \(version.versionNumber)")
                                        .font(.callout.weight(.semibold))
                                    Text(version.createdAt.formatted(.dateTime.month().day().year().hour().minute()))
                                        .font(.caption)
                                        .foregroundStyle(SDColors.textMuted)
                                    if let changeSummary = version.changeSummary, !changeSummary.isEmpty {
                                        Text(changeSummary)
                                            .font(.caption)
                                            .foregroundStyle(SDColors.textSecondary)
                                            .lineLimit(2)
                                    }
                                }

                                Spacer()

                                Button("Restore") {
                                    Task {
                                        await viewModel.restoreVersion(version)
                                    }
                                }
                                .buttonStyle(.borderless)
                                .font(.caption.weight(.semibold))
                            }
                            .padding(10)
                            .background(SDColors.elevatedSurface.opacity(0.52), in: .rect(cornerRadius: 8))
                        }
                    }
                }
            }
        }
    }
}

private struct DocumentIdentityFields: View {
    @Bindable var viewModel: DocumentationEditorViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Document")
                .font(.headline)
                .foregroundStyle(SDColors.textPrimary)
            EditorTextField(label: "Title", text: $viewModel.title, prompt: "Loyalty Rewards Flow")
            EditorTextField(label: "Summary", text: $viewModel.summary, prompt: "One sentence that helps readers understand the document.", axis: .vertical)
        }
    }
}

private struct DocumentAttributeEditor: View {
    @Bindable var viewModel: DocumentationEditorViewModel

    var body: some View {
        SDMetadataPanel("Attributes") {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    SDStatusBadge(title: viewModel.status.displayName, tint: statusTint(viewModel.status))
                    Spacer()
                }

                LazyVGrid(columns: [GridItem(.adaptive(minimum: 210), spacing: 14)], alignment: .leading, spacing: 14) {
                    EditorTextField(label: "Owner", text: $viewModel.owner, prompt: "Person responsible")
                    EditorTextField(label: "Module", text: $viewModel.module, prompt: "Loyalty, Payments, Auth")
                    EditorTextField(label: "Team", text: $viewModel.team, prompt: "Android Shell App")
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Status")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.textMuted)
                    StatusChipSelector(selection: $viewModel.status, tint: statusTint)
                }

                VStack(alignment: .leading, spacing: 8) {
                    EditorTextField(label: "Tags", text: $viewModel.tagsText, prompt: "android, loyalty, qa")
                    if viewModel.tags.isEmpty {
                        Text("Separate tags with commas.")
                            .font(.caption)
                            .foregroundStyle(SDColors.textMuted)
                    } else {
                        SDMetadataTagGroup(tags: viewModel.tags)
                    }
                }
            }
        }
    }

    private func statusTint(_ status: DocumentationStatus) -> Color {
        switch status {
        case .draft: SDColors.textMuted
        case .published: SDColors.success
        case .updatesPending: SDColors.warning
        case .conflicted, .locked: SDColors.warning
        case .archived: SDColors.textMuted
        case .deletedSource: SDColors.danger
        case .outdated: SDColors.danger
        }
    }
}

private struct EditorTextField: View {
    let label: String
    @Binding var text: String
    let prompt: String
    var axis: Axis = .horizontal

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption.weight(.semibold))
                .foregroundStyle(SDColors.textMuted)
            TextField(prompt, text: $text, axis: axis)
                .textFieldStyle(.plain)
                .foregroundStyle(SDColors.textPrimary)
                .padding(.horizontal, 10)
                .padding(.vertical, 9)
                .background(SDColors.elevatedSurface.opacity(0.68), in: .rect(cornerRadius: 8))
                .overlay {
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(SDColors.border.opacity(0.72), lineWidth: 1)
                }
        }
    }
}

private struct StatusChipSelector: View {
    @Binding var selection: DocumentationStatus
    let tint: (DocumentationStatus) -> Color

    var body: some View {
        FlowLayout(spacing: 8) {
            ForEach(DocumentationStatus.allCases, id: \.self) { status in
                let isSelected = status == selection
                Button {
                    selection = status
                } label: {
                    Text(status.displayName)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(isSelected ? SDColors.background : tint(status))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background(isSelected ? tint(status) : tint(status).opacity(0.14), in: .capsule)
                        .overlay {
                            Capsule()
                                .strokeBorder(isSelected ? tint(status).opacity(0.9) : tint(status).opacity(0.22), lineWidth: 1)
                        }
                        .contentShape(.capsule)
                }
                .buttonStyle(.plain)
                .contentShape(.capsule)
                .accessibilityLabel("Status \(status.displayName)")
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
    }
}

private struct FlowLayout: Layout {
    let spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let rows = rows(proposal: proposal, subviews: subviews)
        return CGSize(
            width: proposal.width ?? rows.map(\.width).max() ?? 0,
            height: rows.reduce(0) { $0 + $1.height } + CGFloat(max(0, rows.count - 1)) * spacing
        )
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var y = bounds.minY
        for row in rows(proposal: ProposedViewSize(width: bounds.width, height: nil), subviews: subviews) {
            var x = bounds.minX
            for item in row.items {
                item.subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(item.size))
                x += item.size.width + spacing
            }
            y += row.height + spacing
        }
    }

    private func rows(proposal: ProposedViewSize, subviews: Subviews) -> [Row] {
        let maxWidth = proposal.width ?? .infinity
        var rows: [Row] = []
        var current = Row()

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if current.width + size.width + (current.items.isEmpty ? 0 : spacing) > maxWidth, !current.items.isEmpty {
                rows.append(current)
                current = Row()
            }
            current.items.append((subview, size))
            current.width += size.width + (current.items.count == 1 ? 0 : spacing)
            current.height = max(current.height, size.height)
        }

        if !current.items.isEmpty {
            rows.append(current)
        }

        return rows
    }

    private struct Row {
        var items: [(subview: LayoutSubview, size: CGSize)] = []
        var width: CGFloat = 0
        var height: CGFloat = 0
    }
}
