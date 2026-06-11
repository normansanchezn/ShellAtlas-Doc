import SwiftUI

public struct SDTagView: View {
    private let title: String

    public init(_ title: String) {
        self.title = title
    }

    public var body: some View {
        Text("#\(title)")
            .font(.caption.weight(.medium))
            .foregroundStyle(SDColors.textSecondary)
            .lineLimit(1)
            .truncationMode(.tail)
            .padding(.horizontal, 8)
            .frame(height: 26)
            .frame(maxWidth: 170, alignment: .leading)
            .background(SDColors.elevatedSurface.opacity(0.84), in: .capsule)
            .overlay {
                Capsule()
                    .strokeBorder(SDColors.border.opacity(0.65), lineWidth: 1)
            }
            .fixedSize(horizontal: false, vertical: true)
    }
}
