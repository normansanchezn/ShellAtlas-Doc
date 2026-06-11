import SwiftUI
import SD_Domain
import DS_Core
import SD_DesignSystem

struct DocumentDetailView: View {
    let documentID: String
    @Environment(\.appServices) private var services
    @State private var viewModel = DocumentDetailViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading document…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let document = viewModel.document {
                documentContent(document)
            } else if let error = viewModel.errorMessage {
                ContentUnavailableView("Error", systemImage: "xmark.circle", description: Text(error))
            }
        }
        .navigationTitle(viewModel.document?.title ?? "Document")
        .onAppear {
            Task {
                guard let services else { return }
                try await viewModel.load(documentID: documentID, services: services)
            }
        }
    }

    @ViewBuilder
    private func documentContent(_ doc: KnowledgeDocument) -> some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 20) {
                headerSection(doc)
                if let health = viewModel.healthResult {
                    healthSection(health)
                }
                metadataSection(doc)
                summarySection(doc)
                contentSection(doc)
                if !viewModel.relatedSignals.isEmpty {
                    signalsSection
                }
                if let diagram = doc.mermaidDiagram {
                    diagramSection(diagram)
                }
                if !doc.openAIQuestions.isEmpty {
                    questionsSection(doc)
                }
            }
            .padding()
        }
    }

    private func headerSection(_ doc: KnowledgeDocument) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                SDBadge(label: doc.type.displayName, color: .blue, textColor: .accentColor)
                SDStatusChip(status: doc.status)
                SDBadge(label: doc.platform.displayName, color: .secondary.opacity(0.8), textColor: .secondary)
                Spacer()
            }
            Label(doc.area, systemImage: "building.2.fill")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }

    private func healthSection(_ health: DocumentHealthResult) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Document Health")
                .font(.headline)
            SDHealthPanel(result: health)
        }
    }

    private func metadataSection(_ doc: KnowledgeDocument) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Metadata")
                .font(.headline)
            VStack(spacing: 4) {
                SDMetadataRow(label: "Owner", value: doc.owner, icon: "person.fill")
                SDMetadataRow(label: "Contact", value: doc.mainContact, icon: "person.crop.circle")
                SDMetadataRow(label: "Confidence", value: doc.confidence.displayName, icon: "chart.bar.fill")
                SDMetadataRow(label: "Last Validated", value: doc.lastValidated.formatted(date: .abbreviated, time: .omitted))
                SDMetadataRow(label: "Next Review", value: doc.nextReview.formatted(date: .abbreviated, time: .omitted))
                SDMetadataRow(label: "Review Cycle", value: doc.reviewFrequency.displayName)
                SDMetadataRow(label: "AI Priority", value: doc.aiReviewPriority.displayName)
                if !doc.branches.isEmpty {
                    SDMetadataRow(label: "Branches", value: doc.branches.joined(separator: ", "), icon: "arrow.triangle.branch")
                }
                if !doc.relatedTools.isEmpty {
                    SDMetadataRow(label: "Tools", value: doc.relatedTools.joined(separator: ", "), icon: "wrench.and.screwdriver.fill")
                }
            }
            .padding()
            .background(.background.secondary, in: .rect(cornerRadius: 12))
        }
    }

    private func summarySection(_ doc: KnowledgeDocument) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Summary")
                .font(.headline)
            Text(doc.summary)
                .font(.body)
                .foregroundStyle(.secondary)
        }
    }

    private func contentSection(_ doc: KnowledgeDocument) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Content")
                .font(.headline)
            Text(doc.content)
                .font(.body)
                .padding()
                .background(.background.secondary, in: .rect(cornerRadius: 12))
        }
    }

    private var signalsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Related Signals (\(viewModel.relatedSignals.count))")
                .font(.headline)
            ForEach(viewModel.relatedSignals) { signal in
                HStack(spacing: 10) {
                    Image(systemName: signalIcon(signal))
                        .foregroundStyle(signalColor(signal))
                        .frame(width: 20)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(signal.title)
                            .font(.caption.weight(.medium))
                            .lineLimit(2)
                        Text(signal.description)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                        Text(signal.date.formatted(date: .abbreviated, time: .omitted))
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }
                    Spacer()
                }
                .padding(.vertical, 4)
            }
        }
    }

    private func diagramSection(_ diagram: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Process Diagram (Mermaid)")
                .font(.headline)
            ScrollView(.horizontal, showsIndicators: false) {
                Text(diagram)
                    .font(.system(.caption, design: .monospaced))
                    .padding()
                    .background(.background.secondary, in: .rect(cornerRadius: 8))
            }
        }
    }

    private func questionsSection(_ doc: KnowledgeDocument) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label("Open AI Questions (\(doc.openAIQuestions.count))", systemImage: "questionmark.circle.fill")
                .font(.headline)
                .foregroundStyle(.orange)
            ForEach(doc.openAIQuestions, id: \.self) { question in
                HStack(alignment: .top, spacing: 6) {
                    Image(systemName: "questionmark.circle")
                        .foregroundStyle(.orange)
                        .frame(width: 16)
                    Text(question)
                        .font(.caption)
                }
            }
        }
    }

    private func signalIcon(_ signal: KnowledgeSignal) -> String {
        switch signal.type {
        case .ticket: "ticket.fill"
        case .commit: "arrow.triangle.branch"
        case .release: "tag.fill"
        case .workflow: "gearshape.2.fill"
        }
    }

    private func signalColor(_ signal: KnowledgeSignal) -> Color {
        switch signal.type {
        case .ticket: .blue
        case .commit: .green
        case .release: .purple
        case .workflow: .orange
        }
    }
}
