import SwiftUI

public struct SDTreeNodeRow: View {
    private let title: String
    private let systemImage: String
    private let depth: Int
    private let isSelected: Bool
    private let isExpanded: Bool?
    private let action: () -> Void
    @State private var isHovering = false

    public init(
        title: String,
        systemImage: String,
        depth: Int,
        isSelected: Bool,
        isExpanded: Bool? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.systemImage = systemImage
        self.depth = depth
        self.isSelected = isSelected
        self.isExpanded = isExpanded
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            HStack(spacing: 7) {
                Spacer()
                    .frame(width: CGFloat(depth) * 14)
                if let isExpanded {
                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.textSecondary)
                        .frame(width: 12)
                } else {
                    Spacer()
                        .frame(width: 12)
                }
                Image(systemName: systemImage)
                    .font(.callout)
                    .foregroundStyle(isExpanded == nil ? SDColors.textSecondary : SDColors.primary)
                    .frame(width: 18)
                Text(title)
                    .font(.callout)
                    .lineLimit(1)
                    .foregroundStyle(SDColors.textPrimary)
                Spacer(minLength: 0)
            }
            .frame(maxWidth: .infinity, minHeight: 34, alignment: .leading)
            .contentShape(.rect)
            .padding(.horizontal, 8)
            .padding(.vertical, 7)
            .background(rowBackground, in: .rect(cornerRadius: 7))
        }
        .buttonStyle(.plain)
        .frame(maxWidth: .infinity, alignment: .leading)
        .contentShape(.rect)
        .onHover { hovering in
            isHovering = hovering
        }
        .animation(.easeOut(duration: 0.14), value: isHovering)
        .animation(.easeOut(duration: 0.14), value: isSelected)
    }

    private var rowBackground: Color {
        if isSelected {
            return SDColors.primary.opacity(0.18)
        }
        if isHovering {
            return SDColors.secondary.opacity(0.1)
        }
        return .clear
    }
}
