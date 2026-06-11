import SwiftUI

public struct SDTopBar<TrailingContent: View>: View {
    private let title: String
    private let trailingContent: TrailingContent

    public init(title: String, @ViewBuilder trailingContent: () -> TrailingContent) {
        self.title = title
        self.trailingContent = trailingContent()
    }

    public var body: some View {
        HStack(spacing: 16) {
            Text(title)
                .font(.title3.weight(.bold))
                .foregroundStyle(SDColors.textPrimary)
                .lineLimit(1)
            Spacer(minLength: 16)
            trailingContent
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 12)
        .background(.regularMaterial)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(SDColors.border.opacity(0.75))
                .frame(height: 1)
        }
    }
}

public struct SDGlassIconButton: View {
    private let systemImage: String
    private let accessibilityLabel: String
    private let action: () -> Void
    @State private var isHovering = false

    public init(
        systemImage: String,
        accessibilityLabel: String,
        action: @escaping () -> Void
    ) {
        self.systemImage = systemImage
        self.accessibilityLabel = accessibilityLabel
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(SDColors.textPrimary)
                .frame(width: 34, height: 34)
                .background(.regularMaterial, in: .circle)
                .overlay {
                    Circle()
                        .strokeBorder(SDColors.border.opacity(isHovering ? 0.9 : 0.62), lineWidth: 1)
                }
                .shadow(color: SDColors.background.opacity(isHovering ? 0.24 : 0.14), radius: isHovering ? 14 : 9, x: 0, y: 6)
        }
        .buttonStyle(.plain)
        .contentShape(.circle)
        .accessibilityLabel(accessibilityLabel)
        .onHover { hovering in
            isHovering = hovering
        }
        .animation(.easeOut(duration: 0.14), value: isHovering)
    }
}
