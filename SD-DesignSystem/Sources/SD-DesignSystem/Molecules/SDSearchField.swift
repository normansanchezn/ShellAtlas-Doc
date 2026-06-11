import SwiftUI

public struct SDSearchField: View {
    @Binding private var text: String
    private let placeholder: String
    private let onClear: () -> Void

    public init(
        text: Binding<String>,
        placeholder: String = "Search...",
        onClear: @escaping () -> Void = {}
    ) {
        self._text = text
        self.placeholder = placeholder
        self.onClear = onClear
    }

    public var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(SDColors.textSecondary)
            TextField(placeholder, text: $text)
                .textFieldStyle(.plain)
                .autocorrectionDisabled()
                .foregroundStyle(SDColors.textPrimary)
            if !text.isEmpty {
                Button {
                    text = ""
                    onClear()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundStyle(SDColors.textSecondary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(.thinMaterial, in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.75), lineWidth: 1)
        }
    }
}
