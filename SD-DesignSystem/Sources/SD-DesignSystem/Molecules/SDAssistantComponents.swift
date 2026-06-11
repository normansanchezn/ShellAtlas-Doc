import SwiftUI

public enum SDAssistantMessageRole: Sendable {
    case user
    case assistant
    case system
}

public struct SDAssistantSource: Identifiable, Equatable, Sendable {
    public let id: String
    public let documentId: String
    public let title: String
    public let sectionTitle: String?

    public init(id: String, documentId: String, title: String, sectionTitle: String? = nil) {
        self.id = id
        self.documentId = documentId
        self.title = title
        self.sectionTitle = sectionTitle
    }
}

public struct SDAssistantMessageBubble: View {
    private let role: SDAssistantMessageRole
    private let text: String
    private let sources: [SDAssistantSource]
    private let selectedSourceID: String?
    private let sourcesTitle: String
    private let openSourceAccessibilityPrefix: String
    private let assistantImageName: String?
    private let onSourceSelected: ((SDAssistantSource) -> Void)?

    public init(
        role: SDAssistantMessageRole,
        text: String,
        sources: [SDAssistantSource] = [],
        selectedSourceID: String? = nil,
        sourcesTitle: String = "Sources",
        openSourceAccessibilityPrefix: String = "Open source document",
        assistantImageName: String? = nil,
        onSourceSelected: ((SDAssistantSource) -> Void)? = nil
    ) {
        self.role = role
        self.text = text
        self.sources = sources
        self.selectedSourceID = selectedSourceID
        self.sourcesTitle = sourcesTitle
        self.openSourceAccessibilityPrefix = openSourceAccessibilityPrefix
        self.assistantImageName = assistantImageName
        self.onSourceSelected = onSourceSelected
    }

    public var body: some View {
        HStack(alignment: .top, spacing: 8) {
            if role != .user {
                assistantAvatar
            }

            VStack(alignment: role == .user ? .trailing : .leading, spacing: 8) {
                SDMarkdownBodyView(
                    content: text,
                    textColor: role == .user ? SDColors.background : SDColors.textPrimary,
                    secondaryTextColor: role == .user ? SDColors.background.opacity(0.72) : SDColors.textSecondary
                )
                    .padding(12)
                    .background(backgroundStyle, in: .rect(cornerRadius: 16))

                if !sources.isEmpty {
                    SDAssistantSourcesPanel(
                        sources: sources,
                        selectedSourceID: selectedSourceID,
                        title: sourcesTitle,
                        openSourceAccessibilityPrefix: openSourceAccessibilityPrefix,
                        onSourceSelected: onSourceSelected
                    )
                }
            }
            .fixedSize(horizontal: false, vertical: true)
            .frame(maxWidth: role == .user ? 560 : 760, alignment: role == .user ? .trailing : .leading)
        }
        .frame(maxWidth: .infinity, alignment: role == .user ? .trailing : .leading)
    }

    @ViewBuilder
    private var assistantAvatar: some View {
        if role == .system {
            Image(systemName: "info.circle.fill")
                .foregroundStyle(SDColors.textSecondary)
                .frame(width: 24)
                .padding(.top, 2)
        } else if let assistantImageName {
            Image(assistantImageName)
                .resizable()
                .scaledToFit()
                .frame(width: 24, height: 24)
                .clipShape(.rect(cornerRadius: 6))
                .padding(.top, 2)
                .accessibilityHidden(true)
        } else {
            Image(systemName: "sparkles")
                .foregroundStyle(SDColors.secondary)
                .frame(width: 24)
                .padding(.top, 2)
        }
    }

    private var backgroundStyle: Color {
        switch role {
        case .user:
            SDColors.primary
        case .assistant:
            SDColors.surface.opacity(0.78)
        case .system:
            SDColors.secondary.opacity(0.12)
        }
    }
}

public struct SDAssistantInputBar: View {
    @Binding private var text: String
    private let placeholder: String
    private let isSending: Bool
    private let send: () -> Void

    public init(
        text: Binding<String>,
        placeholder: String = "Ask a question...",
        isSending: Bool,
        send: @escaping () -> Void
    ) {
        self._text = text
        self.placeholder = placeholder
        self.isSending = isSending
        self.send = send
    }

    public var body: some View {
        HStack(spacing: 8) {
            TextField(placeholder, text: $text, axis: .vertical)
                .lineLimit(1...4)
                .textFieldStyle(.plain)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .foregroundStyle(SDColors.textPrimary)
                .background(.regularMaterial, in: .rect(cornerRadius: 20))
                .overlay {
                    RoundedRectangle(cornerRadius: 20)
                        .strokeBorder(SDColors.border, lineWidth: 1)
                }
                .onSubmit(send)

            Button(action: send) {
                Image(systemName: isSending ? "hourglass.circle.fill" : "arrow.up.circle.fill")
                    .font(.title2)
                    .foregroundStyle(canSend ? AnyShapeStyle(SDColors.primary) : AnyShapeStyle(SDColors.textSecondary))
            }
            .buttonStyle(.plain)
            .disabled(!canSend)
            .help("Send question")
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(SDColors.background)
    }

    private var canSend: Bool {
        !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isSending
    }
}

public struct SDAssistantSourceChip: View {
    private let source: SDAssistantSource
    private let isSelected: Bool
    private let accessibilityPrefix: String
    private let action: ((SDAssistantSource) -> Void)?

    public init(
        source: SDAssistantSource,
        isSelected: Bool = false,
        accessibilityPrefix: String = "Open source document",
        action: ((SDAssistantSource) -> Void)? = nil
    ) {
        self.source = source
        self.isSelected = isSelected
        self.accessibilityPrefix = accessibilityPrefix
        self.action = action
    }

    public var body: some View {
        Button {
            action?(source)
        } label: {
            HStack(spacing: 5) {
                Image(systemName: "doc.text.fill")
                    .font(.caption2)
                VStack(alignment: .leading, spacing: 1) {
                    Text(source.title)
                        .font(.caption.weight(.semibold))
                        .lineLimit(1)
                    Text(source.documentId)
                        .font(.caption2)
                        .foregroundStyle(SDColors.textSecondary)
                        .lineLimit(1)
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 6)
            .background(isSelected ? SDColors.primary.opacity(0.16) : SDColors.secondary.opacity(0.12), in: .rect(cornerRadius: 8))
            .overlay {
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(isSelected ? SDColors.primary.opacity(0.65) : SDColors.secondary.opacity(0.16), lineWidth: 1)
            }
            .foregroundStyle(isSelected ? SDColors.primary : SDColors.secondary)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .contentShape(.rect(cornerRadius: 8))
        .disabled(action == nil)
        .accessibilityLabel("\(accessibilityPrefix) \(source.title)")
    }
}

public struct SDAssistantSourcesPanel: View {
    private let sources: [SDAssistantSource]
    private let selectedSourceID: String?
    private let title: String
    private let openSourceAccessibilityPrefix: String
    private let onSourceSelected: ((SDAssistantSource) -> Void)?

    public init(
        sources: [SDAssistantSource],
        selectedSourceID: String? = nil,
        title: String = "Sources",
        openSourceAccessibilityPrefix: String = "Open source document",
        onSourceSelected: ((SDAssistantSource) -> Void)? = nil
    ) {
        self.sources = sources
        self.selectedSourceID = selectedSourceID
        self.title = title
        self.openSourceAccessibilityPrefix = openSourceAccessibilityPrefix
        self.onSourceSelected = onSourceSelected
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption.weight(.semibold))
                .foregroundStyle(SDColors.textSecondary)

            FlowLayout(spacing: 6) {
                ForEach(sources) { source in
                    SDAssistantSourceChip(
                        source: source,
                        isSelected: source.id == selectedSourceID,
                        accessibilityPrefix: openSourceAccessibilityPrefix,
                        action: onSourceSelected
                    )
                }
            }
        }
        .padding(10)
        .background(.thinMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.secondary.opacity(0.16), lineWidth: 1)
        }
    }
}

public struct SDLocalModelStatusBadge: View {
    private let title: String
    private let isAvailable: Bool

    public init(title: String, isAvailable: Bool) {
        self.title = title
        self.isAvailable = isAvailable
    }

    public var body: some View {
        HStack(spacing: 6) {
            Image(systemName: isAvailable ? "checkmark.circle.fill" : "arrow.clockwise.circle.fill")
                .imageScale(.small)
            Text(title)
        }
            .font(.caption.weight(.semibold))
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background((isAvailable ? SDColors.success : SDColors.warning).opacity(0.12), in: .rect(cornerRadius: 8))
            .foregroundStyle(isAvailable ? SDColors.success : SDColors.warning)
            .accessibilityLabel(isAvailable ? "\(title), available" : "\(title), retry connection")
    }
}

public struct SDTypingIndicator: View {
    private let assistantImageName: String?

    public init(assistantImageName: String? = nil) {
        self.assistantImageName = assistantImageName
    }

    public var body: some View {
        HStack(spacing: 8) {
            if let assistantImageName {
                Image(assistantImageName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
                    .clipShape(.rect(cornerRadius: 6))
                    .accessibilityHidden(true)
            } else {
                Image(systemName: "sparkles")
                    .foregroundStyle(SDColors.secondary)
                    .frame(width: 24)
            }
            HStack(spacing: 4) {
                ForEach(0..<3, id: \.self) { index in
                    Circle()
                        .fill(SDColors.textSecondary)
                        .frame(width: 6, height: 6)
                        .opacity(index == 1 ? 0.7 : 1)
                }
            }
            .padding(12)
            .background(.regularMaterial, in: .rect(cornerRadius: 16))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
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
            if current.width + size.width + spacing > maxWidth, !current.items.isEmpty {
                rows.append(current)
                current = Row()
            }
            current.add(subview: subview, size: size, spacing: spacing)
        }

        if !current.items.isEmpty {
            rows.append(current)
        }
        return rows
    }

    private struct Row {
        var items: [Item] = []
        var width: CGFloat = 0
        var height: CGFloat = 0

        mutating func add(subview: LayoutSubview, size: CGSize, spacing: CGFloat) {
            if !items.isEmpty {
                width += spacing
            }
            items.append(Item(subview: subview, size: size))
            width += size.width
            height = max(height, size.height)
        }
    }

    private struct Item {
        let subview: LayoutSubview
        let size: CGSize
    }
}
