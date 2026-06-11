import SwiftUI

public struct SDPrimaryButton: View {
    private let title: String
    private let systemImage: String?
    private let isDisabled: Bool
    private let action: () -> Void

    public init(
        _ title: String,
        systemImage: String? = nil,
        isDisabled: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.systemImage = systemImage
        self.isDisabled = isDisabled
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            Label {
                Text(title)
            } icon: {
                if let systemImage {
                    Image(systemName: systemImage)
                }
            }
            .font(.callout.weight(.semibold))
            .padding(.horizontal, 14)
            .padding(.vertical, 9)
            .frame(minHeight: 36)
            .lineLimit(1)
            .minimumScaleFactor(0.82)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .foregroundStyle(SDColors.background)
        .background(isDisabled ? SDColors.border.opacity(0.7) : SDColors.primary, in: .rect(cornerRadius: 8))
        .contentShape(.rect(cornerRadius: 8))
        .disabled(isDisabled)
    }
}
