import SwiftUI
import DS_Core
import SD_DesignSystem

struct LoggerPanelView: View {
    @Binding var isPresented: Bool
    @State private var store = ShellDocEventLogStore.shared
    @State private var selectedScope: LoggerScopeFilter = .all
    @State private var followLatest = true

    var body: some View {
        VStack(spacing: 0) {
            header
            Divider()
                .overlay(SDColors.border.opacity(0.7))
            scopeBar
            Divider()
                .overlay(SDColors.border.opacity(0.7))
            logList
        }
        .frame(maxHeight: .infinity)
        .background(SDColors.topBarBackground.opacity(0.98))
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .strokeBorder(SDColors.border.opacity(0.65), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .shadow(color: .black.opacity(0.28), radius: 24, x: -6, y: 0)
    }

    private var header: some View {
        HStack(spacing: 12) {
            Image(systemName: "terminal.fill")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(SDColors.primary)
                .frame(width: 28, height: 28)
                .background(SDColors.accentSoft, in: Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text("Logger")
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                Text("\(visibleEntries.count) entries")
                    .font(.caption)
                    .foregroundStyle(SDColors.textMuted)
            }

            Spacer()

            Toggle(isOn: $followLatest) {
                Text("Follow")
            }
            .labelsHidden()
            .toggleStyle(.switch)
            .tint(SDColors.primary)

            Button {
                store.clear()
            } label: {
                Image(systemName: "trash")
                    .font(.system(size: 13, weight: .semibold))
                    .frame(width: 30, height: 30)
                    .foregroundStyle(SDColors.textSecondary)
            }
            .buttonStyle(.plain)
            .help("Clear logs")

            Button {
                isPresented = false
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 13, weight: .semibold))
                    .frame(width: 30, height: 30)
                    .foregroundStyle(SDColors.textSecondary)
            }
            .buttonStyle(.plain)
            .help("Close logs")
        }
        .padding(16)
    }

    private var scopeBar: some View {
        Picker("Scope", selection: $selectedScope) {
            ForEach(LoggerScopeFilter.allCases) { scope in
                Text(scope.title).tag(scope)
            }
        }
        .pickerStyle(.segmented)
        .padding(16)
    }

    private var logList: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 12) {
                    ForEach(visibleEntries) { entry in
                        LogEntryCard(entry: entry)
                            .id(entry.id)
                    }
                }
                .padding(16)
            }
            .onChange(of: visibleEntries.count) { _, _ in
                guard followLatest, let lastID = visibleEntries.last?.id else { return }
                withAnimation(.easeOut(duration: 0.2)) {
                    proxy.scrollTo(lastID, anchor: .bottom)
                }
            }
            .onAppear {
                guard followLatest, let lastID = visibleEntries.last?.id else { return }
                DispatchQueue.main.async {
                    proxy.scrollTo(lastID, anchor: .bottom)
                }
            }
        }
    }

    private var visibleEntries: [ShellDocLogEntry] {
        store.entries.filter { selectedScope.matches(scope: $0.scope) }
    }
}

private struct LogEntryCard: View {
    let entry: ShellDocLogEntry

    private static let formatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .none
        formatter.timeStyle = .medium
        return formatter
    }()

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .firstTextBaseline, spacing: 10) {
                Text(entry.event)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)

                Text(entry.scope)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(SDColors.textMuted)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(SDColors.surface.opacity(0.9), in: Capsule())

                Spacer()

                Text(Self.formatter.string(from: entry.timestamp))
                    .font(.caption)
                    .foregroundStyle(SDColors.textMuted)
            }

            Text(entry.prettyJSON)
                .font(.system(size: 11, design: .monospaced))
                .foregroundStyle(SDColors.textSecondary)
                .textSelection(.enabled)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SDColors.surface.opacity(0.82), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(SDColors.border.opacity(0.6), lineWidth: 1)
        )
    }
}

private enum LoggerScopeFilter: String, CaseIterable, Identifiable {
    case all
    case connection
    case backend
    case ollama

    var id: String { rawValue }

    var title: String {
        switch self {
        case .all: "All"
        case .connection: "Connection"
        case .backend: "Backend"
        case .ollama: "Ollama"
        }
    }

    func matches(scope: String) -> Bool {
        switch self {
        case .all:
            return true
        case .connection:
            return scope.contains("connection")
        case .backend:
            return scope.contains("remote")
        case .ollama:
            return scope.contains("ollama")
        }
    }
}
