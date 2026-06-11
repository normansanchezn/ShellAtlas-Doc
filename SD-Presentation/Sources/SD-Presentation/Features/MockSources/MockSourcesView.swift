import SwiftUI
import SD_DesignSystem
import SD_Domain
import DS_Core

struct MockSourcesView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = MockSourcesViewModel()
    @State private var selectedTab = 0

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading mock sources…")
            } else {
                TabView(selection: $selectedTab) {
                    Tab("Documents", systemImage: "doc.fill", value: 0) {
                        documentsTab
                    }
                    Tab("Tickets", systemImage: "ticket.fill", value: 1) {
                        ticketsTab
                    }
                    Tab("Commits", systemImage: "arrow.triangle.branch", value: 2) {
                        commitsTab
                    }
                    Tab("Workflows", systemImage: "gearshape.2.fill", value: 3) {
                        workflowsTab
                    }
                    Tab("Releases", systemImage: "tag.fill", value: 4) {
                        releasesTab
                    }
                }
                .tabViewStyle(.automatic)
            }
        }
        .navigationTitle("Mock Sources")
        .onAppear {
            Task {
                guard let services else { return }
                try await viewModel.load(services: services)
            }
        }
    }

    private var documentsTab: some View {
        List(viewModel.documents) { doc in
            VStack(alignment: .leading, spacing: 4) {
                Text(doc.title).font(.subheadline.weight(.medium))
                Text("\(doc.type.displayName) · \(doc.platform.displayName)")
                    .font(.caption).foregroundStyle(.secondary)
                Text("Owner: \(doc.owner)")
                    .font(.caption2).foregroundStyle(.tertiary)
            }
            .padding(.vertical, 2)
        }
        .listStyle(.plain)
    }

    private var ticketsTab: some View {
        List(viewModel.tickets) { ticket in
            VStack(alignment: .leading, spacing: 4) {
                Text(ticket.title).font(.subheadline.weight(.medium))
                HStack {
                    SDBadge(label: ticket.type.displayName, color: .blue, textColor: .accentColor)
                    SDBadge(label: ticket.status.displayName, color: ticket.status == .closed ? .green : .orange, textColor: .yellow)
                }
                Text(ticket.description).font(.caption).foregroundStyle(.secondary).lineLimit(2)
            }
            .padding(.vertical, 2)
        }
        .listStyle(.plain)
    }

    private var commitsTab: some View {
        List(viewModel.commits) { commit in
            VStack(alignment: .leading, spacing: 4) {
                Text(commit.message).font(.caption.weight(.medium)).lineLimit(2)
                Text("[\(commit.sha.prefix(7))] \(commit.author) — \(commit.repository)")
                    .font(.caption2).foregroundStyle(.secondary)
                Text(commit.date.formatted(date: .abbreviated, time: .shortened))
                    .font(.caption2).foregroundStyle(.tertiary)
            }
            .padding(.vertical, 2)
        }
        .listStyle(.plain)
    }

    private var workflowsTab: some View {
        List(viewModel.workflows) { wf in
            VStack(alignment: .leading, spacing: 4) {
                Text(wf.workflowName).font(.subheadline.weight(.medium))
                Text(wf.description).font(.caption).foregroundStyle(.secondary).lineLimit(2)
                Text("Changed: \(wf.changedAt.formatted(date: .abbreviated, time: .omitted))")
                    .font(.caption2).foregroundStyle(.tertiary)
            }
            .padding(.vertical, 2)
        }
        .listStyle(.plain)
    }

    private var releasesTab: some View {
        List(viewModel.releases) { release in
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text("v\(release.version)").font(.subheadline.weight(.bold))
                    SDBadge(label: release.platform.displayName, color: .purple, textColor: .primary)
                }
                ForEach(release.highlights, id: \.self) { h in
                    Text("• \(h)").font(.caption).foregroundStyle(.secondary)
                }
            }
            .padding(.vertical, 2)
        }
        .listStyle(.plain)
    }
}

#Preview {
    NavigationStack {
        MockSourcesView()
    }
}
