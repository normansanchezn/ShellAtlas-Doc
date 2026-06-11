import SwiftUI
import SD_DesignSystem
import SD_Domain

struct DashboardView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = DashboardViewModel()

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 22) {
                if viewModel.isLoading {
                    SDLoadingView("Loading dashboard...")
                        .frame(minHeight: 260)
                } else if let errorMessage = viewModel.errorMessage {
                    SDErrorStateView(message: errorMessage) {
                        load()
                    }
                    .frame(minHeight: 260)
                } else {
                    metricsSection
                    chartsGrid
                }
            }
            .padding(24)
            .frame(maxWidth: 1180)
            .frame(maxWidth: .infinity, alignment: .center)
        }
        .scrollContentBackground(.hidden)
        .background(SDColors.background)
        .navigationTitle("Dashboard")
        .task {
            load()
        }
    }

    private var metricsSection: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 210), spacing: 14)], spacing: 14) {
            DashboardMetricCard(
                title: "Docs desactualizados",
                value: "\(viewModel.outdatedDocuments)",
                systemImage: "exclamationmark.triangle.fill",
                color: SDColors.warning
            )
            DashboardMetricCard(
                title: "Docs actualizados",
                value: "\(viewModel.updatedDocuments)",
                systemImage: "checkmark.seal.fill",
                color: SDColors.success
            )
            DashboardMetricCard(
                title: "Pendientes de update",
                value: "\(viewModel.pendingUpdateDocuments)",
                systemImage: "clock.badge.exclamationmark.fill",
                color: SDColors.secondary
            )
            DashboardMetricCard(
                title: "Total documentos",
                value: "\(viewModel.totalDocuments)",
                systemImage: "doc.text.fill",
                color: SDColors.primary
            )
        }
    }

    private var chartsGrid: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 340), spacing: 16)], spacing: 16) {
            ModuleBarChartCard(
                title: "Módulos desactualizados",
                subtitle: "Módulos con más documentos vencidos o pendientes.",
                metrics: viewModel.outdatedModules,
                color: SDColors.warning
            )

            ModuleBarChartCard(
                title: "Módulos más actualizados",
                subtitle: "Módulos con más documentos publicados al día.",
                metrics: viewModel.updatedModules,
                color: SDColors.success
            )

            ReviewedDocumentsCard(documents: viewModel.mostReviewedDocuments)
        }
    }

    private func load() {
        Task {
            guard let services else { return }
            try? await viewModel.load(services: services)
        }
    }
}

private struct DashboardMetricCard: View {
    let title: String
    let value: String
    let systemImage: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Image(systemName: systemImage)
                    .font(.title3)
                    .foregroundStyle(color)
                    .frame(width: 30, height: 30)
                    .background(color.opacity(0.14), in: .rect(cornerRadius: 7))
                Spacer()
            }

            Text(value)
                .font(.system(size: 34, weight: .bold, design: .rounded))
                .foregroundStyle(SDColors.textPrimary)
            Text(title)
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textSecondary)
        }
        .padding(16)
        .background(.regularMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.75), lineWidth: 1)
        }
    }
}

private struct ModuleBarChartCard: View {
    let title: String
    let subtitle: String
    let metrics: [DashboardModuleMetric]
    let color: Color

    private var maxCount: Int {
        max(metrics.map(\.count).max() ?? 1, 1)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            chartHeader

            if metrics.isEmpty {
                SDEmptyStateView("No data", systemImage: "chart.bar.xaxis", message: "There are no documents for this category.")
                    .frame(minHeight: 180)
            } else {
                VStack(spacing: 12) {
                    ForEach(metrics) { metric in
                        moduleRow(metric)
                    }
                }
            }
        }
        .padding(16)
        .background(.regularMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.75), lineWidth: 1)
        }
    }

    private var chartHeader: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.headline.weight(.semibold))
                .foregroundStyle(SDColors.textPrimary)
            Text(subtitle)
                .font(.caption)
                .foregroundStyle(SDColors.textSecondary)
        }
    }

    private func moduleRow(_ metric: DashboardModuleMetric) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(metric.module)
                    .font(.callout.weight(.medium))
                    .foregroundStyle(SDColors.textPrimary)
                    .lineLimit(1)
                Spacer()
                Text("\(metric.count)")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(color)
            }

            GeometryReader { proxy in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(SDColors.border.opacity(0.55))
                    Capsule()
                        .fill(color)
                        .frame(width: proxy.size.width * CGFloat(metric.count) / CGFloat(maxCount))
                }
            }
            .frame(height: 8)
        }
    }
}

private struct ReviewedDocumentsCard: View {
    let documents: [DashboardDocumentReviewMetric]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Docs más revisados")
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                Text("Ordenados por fecha de revisión o actualización más reciente.")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            }

            if documents.isEmpty {
                SDEmptyStateView("No data", systemImage: "doc.text.magnifyingglass", message: "No reviewed documents are available.")
                    .frame(minHeight: 180)
            } else {
                VStack(spacing: 0) {
                    ForEach(documents) { document in
                        reviewedRow(document)
                        if document.id != documents.last?.id {
                            Rectangle()
                                .fill(SDColors.border.opacity(0.65))
                                .frame(height: 1)
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(.regularMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.75), lineWidth: 1)
        }
    }

    private func reviewedRow(_ document: DashboardDocumentReviewMetric) -> some View {
        HStack(spacing: 12) {
            Image(systemName: "doc.text.fill")
                .foregroundStyle(SDColors.secondary)
                .frame(width: 28, height: 28)
                .background(SDColors.secondary.opacity(0.12), in: .rect(cornerRadius: 6))

            VStack(alignment: .leading, spacing: 3) {
                Text(document.title)
                    .font(.callout.weight(.medium))
                    .foregroundStyle(SDColors.textPrimary)
                    .lineLimit(1)
                Text(document.module)
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            }

            Spacer()

            Text(document.lastReviewed.formatted(.dateTime.month().day().year()))
                .font(.caption.weight(.medium))
                .foregroundStyle(SDColors.textSecondary)
        }
        .padding(.vertical, 10)
    }
}

#Preview {
    NavigationStack {
        DashboardView()
    }
}
