import SwiftUI
import SD_Domain
import SD_DesignSystem

struct OutdatedReviewView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = OutdatedReviewViewModel()
    @State private var editorMode: DocumentationEditorMode?

    var body: some View {
        @Bindable var viewModel = viewModel

        HStack(spacing: 0) {
            updatesList(viewModel: viewModel)
                .frame(minWidth: 340, idealWidth: 420, maxWidth: 480)

            Divider()

            updatesDetail(viewModel: viewModel)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .navigationDestination(item: $editorMode) { mode in
            DocumentationEditorView(mode: mode) { documentID in
                Task {
                    await viewModel.refreshAfterSave(documentID: documentID)
                }
            }
        }
        .task {
            guard let services else { return }
            viewModel.configure(services: services)
            await viewModel.loadDocuments()
        }
    }

    private func updatesList(viewModel: OutdatedReviewViewModel) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: "clock.badge.exclamationmark")
                        .foregroundStyle(SDColors.warning)
                    Text("Updates Pending")
                        .font(.title3.weight(.semibold))
                    Spacer()
                    Text("\(viewModel.documents.count)")
                        .font(.caption.weight(.bold))
                        .foregroundStyle(SDColors.textMuted)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(SDColors.elevatedSurface.opacity(0.72), in: .capsule)
                }

                Text("Documents waiting for content updates based on status or stale metadata.")
                    .font(.callout)
                    .foregroundStyle(SDColors.textSecondary)
            }
            .padding(18)

            Divider()

            if viewModel.isLoading {
                SDLoadingView("Loading update queue...")
            } else if let errorMessage = viewModel.errorMessage {
                SDErrorStateView(message: errorMessage) {
                    Task {
                        await viewModel.loadDocuments()
                    }
                }
            } else if viewModel.documents.isEmpty {
                SDEmptyStateView("No Pending Updates", systemImage: "checkmark.seal", message: "All documentation is current.")
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 10) {
                        ForEach(viewModel.documents) { document in
                            UpdatesPendingDocumentCard(
                                document: document,
                                isSelected: viewModel.selectedDocumentID == document.id,
                                status: viewModel.effectiveStatus(for: document),
                                reason: viewModel.updateReason(for: document),
                                onOpen: {
                                    Task {
                                        await viewModel.selectDocument(id: document.id)
                                    }
                                },
                                onEdit: {
                                    editorMode = .edit(documentId: document.id)
                                }
                            )
                        }
                    }
                    .padding(14)
                }
            }
        }
        .background(SDColors.sidebarBackground.opacity(0.92))
    }

    @ViewBuilder
    private func updatesDetail(viewModel: OutdatedReviewViewModel) -> some View {
        if let document = viewModel.selectedDocument {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    HStack(alignment: .top, spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(document.title)
                                .font(.largeTitle.weight(.bold))
                            if !document.summary.isEmpty {
                                Text(document.summary)
                                    .font(.title3)
                                    .foregroundStyle(SDColors.textSecondary)
                            }
                        }

                        Spacer()

                        VStack(alignment: .trailing, spacing: 10) {
                            let status = viewModel.effectiveStatus(for: document)
                            SDStatusBadge(title: status.displayName, tint: statusTint(status))
                            SDPrimaryButton("Edit", systemImage: "pencil") {
                                editorMode = .edit(documentId: document.id)
                            }
                        }
                    }

                    UpdateReasonBanner(reason: viewModel.updateReason(for: document))

                    SDMetadataPanel("Attributes") {
                        DocumentationMetadataGrid(document: document)
                    }

                    SDMetadataPanel("Tags") {
                        SDMetadataTagGroup(tags: document.attributes.tags)
                    }

                    Divider()

                    if let contentJSON = document.contentJSON {
                        SDStructuredDocumentView(contentJSON: contentJSON)
                    } else {
                        MarkdownBodyView(content: document.content)
                    }
                }
                .padding(.horizontal, 36)
                .padding(.vertical, 30)
                .frame(maxWidth: 920, alignment: .topLeading)
                .frame(maxWidth: .infinity, alignment: .topLeading)
            }
            .background(SDColors.contentBackground)
        } else {
            SDEmptyStateView("Select a Document", systemImage: "doc.text.magnifyingglass", message: "Choose a pending update to review its content and attributes.")
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

private struct UpdatesPendingDocumentCard: View {
    let document: DocumentationDocument
    let isSelected: Bool
    let status: DocumentationStatus
    let reason: String
    let onOpen: () -> Void
    let onEdit: () -> Void
    @State private var isHovering = false

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top, spacing: 10) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(document.title)
                        .font(.headline)
                        .lineLimit(2)
                    Text(document.summary)
                        .font(.callout)
                        .foregroundStyle(SDColors.textSecondary)
                        .lineLimit(2)
                }

                Spacer(minLength: 0)

                SDStatusBadge(title: status.displayName, tint: statusTint(status))
            }

            Label(reason, systemImage: "exclamationmark.triangle")
                .font(.caption.weight(.semibold))
                .foregroundStyle(SDColors.warning)
                .lineLimit(2)

            VStack(alignment: .leading, spacing: 5) {
                Label(document.attributes.owner, systemImage: "person.crop.circle")
                Label("\(document.attributes.module) · \(document.attributes.team)", systemImage: "square.stack.3d.up")
                Label(document.attributes.lastUpdated.formatted(.dateTime.month().day().year()), systemImage: "clock")
            }
            .font(.caption)
            .foregroundStyle(SDColors.textMuted)

            if !document.attributes.tags.isEmpty {
                SDMetadataTagGroup(tags: Array(document.attributes.tags.prefix(4)))
            }

            HStack(spacing: 12) {
                Button(action: onOpen) {
                    Label("Open", systemImage: "arrow.right.circle")
                }
                .buttonStyle(.borderless)

                Button(action: onEdit) {
                    Label("Edit", systemImage: "pencil")
                }
                .buttonStyle(.borderless)

                Spacer()
            }
            .font(.caption.weight(.semibold))
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(cardBackground, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(borderColor, lineWidth: isSelected ? 1.5 : 1)
        }
        .contentShape(.rect)
        .onTapGesture(perform: onOpen)
        .onHover { hovering in
            isHovering = hovering
        }
        .animation(.easeOut(duration: 0.14), value: isHovering)
    }

    private var cardBackground: Color {
        if isSelected { return SDColors.accentSoft }
        if isHovering { return SDColors.elevatedSurface.opacity(0.64) }
        return .clear
    }

    private var borderColor: Color {
        if isSelected { return SDColors.primary.opacity(0.82) }
        return SDColors.border.opacity(isHovering ? 0.84 : 0.58)
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

private struct UpdateReasonBanner: View {
    let reason: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "clock.badge.exclamationmark")
                .foregroundStyle(SDColors.warning)
                .padding(.top, 2)

            VStack(alignment: .leading, spacing: 4) {
                Text("Update required")
                    .font(.callout.weight(.semibold))
                Text(reason)
                    .font(.callout)
                    .foregroundStyle(SDColors.textSecondary)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SDColors.warningSoft, in: .rect(cornerRadius: 8))
    }
}

private struct DocumentationMetadataGrid: View {
    let document: DocumentationDocument

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 180), spacing: 14)], alignment: .leading, spacing: 14) {
            SDMetadataItem(label: "Owner", value: document.attributes.owner, systemImage: "person.crop.circle")
            SDMetadataItem(label: "Module", value: document.attributes.module, systemImage: "square.stack.3d.up")
            SDMetadataItem(label: "Team", value: document.attributes.team, systemImage: "person.3")
            SDMetadataItem(label: "Created", value: document.attributes.createdAt.formatted(.dateTime.month().day().year()), systemImage: "calendar.badge.plus")
            SDMetadataItem(label: "Last updated", value: document.attributes.lastUpdated.formatted(.dateTime.month().day().year()), systemImage: "clock.arrow.circlepath")
        }
    }
}

#Preview {
    NavigationStack {
        OutdatedReviewView()
    }
}
