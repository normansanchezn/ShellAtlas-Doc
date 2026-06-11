import SwiftUI

public struct SDStatusBadge: View {
    private let title: String
    private let tint: Color

    public init(title: String, tint: Color) {
        self.title = title
        self.tint = tint
    }

    public var body: some View {
        Text(title)
            .font(.caption.weight(.semibold))
            .foregroundStyle(tint)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(tint.opacity(0.14), in: .capsule)
    }
}
