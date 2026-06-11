import SwiftUI

public struct SDSettingsSection<Content: View>: View {
    private let title: String
    private let systemImage: String
    private let content: Content

    public init(
        _ title: String,
        systemImage: String,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.systemImage = systemImage
        self.content = content()
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(title, systemImage: systemImage)
                .font(.headline.weight(.semibold))
                .foregroundStyle(SDColors.textPrimary)

            VStack(spacing: 0) {
                content
            }
            .background(.regularMaterial, in: .rect(cornerRadius: 8))
            .overlay {
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(SDColors.border.opacity(0.9), lineWidth: 1)
            }
        }
    }
}

public struct SDSettingsRow: View {
    private let title: String
    private let subtitle: String?
    private let value: String?
    private let systemImage: String

    public init(title: String, subtitle: String? = nil, value: String? = nil, systemImage: String) {
        self.title = title
        self.subtitle = subtitle
        self.value = value
        self.systemImage = systemImage
    }

    public var body: some View {
        HStack(spacing: 12) {
            SDSettingsIcon(systemImage: systemImage)
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.callout.weight(.medium))
                    .foregroundStyle(SDColors.textPrimary)
                if let subtitle {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundStyle(SDColors.textSecondary)
                }
            }
            Spacer(minLength: 16)
            if let value {
                Text(value)
                    .font(.callout)
                    .foregroundStyle(SDColors.textSecondary)
                    .multilineTextAlignment(.trailing)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
    }
}

public struct SDSettingsToggleRow: View {
    private let title: String
    private let subtitle: String?
    private let systemImage: String
    @Binding private var isOn: Bool

    public init(title: String, subtitle: String? = nil, systemImage: String, isOn: Binding<Bool>) {
        self.title = title
        self.subtitle = subtitle
        self.systemImage = systemImage
        self._isOn = isOn
    }

    public var body: some View {
        HStack(spacing: 12) {
            SDSettingsIcon(systemImage: systemImage)
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.callout.weight(.medium))
                    .foregroundStyle(SDColors.textPrimary)
                if let subtitle {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundStyle(SDColors.textSecondary)
                }
            }
            Spacer(minLength: 16)
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(SDColors.success)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 9)
    }
}

public struct SDSettingsPickerRow<Selection: Hashable, Content: View>: View {
    private let title: String
    private let systemImage: String
    @Binding private var selection: Selection
    private let content: Content

    public init(
        title: String,
        systemImage: String,
        selection: Binding<Selection>,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.systemImage = systemImage
        self._selection = selection
        self.content = content()
    }

    public var body: some View {
        HStack(spacing: 12) {
            SDSettingsIcon(systemImage: systemImage)
            Text(title)
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textPrimary)
            Spacer(minLength: 16)
            Picker(title, selection: $selection) {
                content
            }
            .labelsHidden()
            .tint(SDColors.primary)
            .frame(maxWidth: 220)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 9)
    }
}

public struct SDSettingsActionRow: View {
    private let title: String
    private let subtitle: String?
    private let systemImage: String
    private let role: ButtonRole?
    private let action: () -> Void

    public init(
        title: String,
        subtitle: String? = nil,
        systemImage: String,
        role: ButtonRole? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.subtitle = subtitle
        self.systemImage = systemImage
        self.role = role
        self.action = action
    }

    public var body: some View {
        Button(role: role, action: action) {
            HStack(spacing: 12) {
                SDSettingsIcon(systemImage: systemImage)
                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .font(.callout.weight(.medium))
                    if let subtitle {
                        Text(subtitle)
                            .font(.caption)
                            .foregroundStyle(SDColors.textSecondary)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(SDColors.textSecondary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 11)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .foregroundStyle(role == .destructive ? SDColors.danger : SDColors.textPrimary)
    }
}

public struct SDSettingsStatusBadge: View {
    private let title: String
    private let style: Style

    public enum Style: Sendable {
        case success
        case info
        case warning
        case muted
    }

    public init(_ title: String, style: Style) {
        self.title = title
        self.style = style
    }

    public var body: some View {
        Text(title)
            .font(.caption2.weight(.semibold))
            .foregroundStyle(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.14), in: .capsule)
    }

    private var color: Color {
        switch style {
        case .success: SDColors.success
        case .info: SDColors.secondary
        case .warning: SDColors.warning
        case .muted: SDColors.textSecondary
        }
    }
}

public struct SDSettingsDivider: View {
    public init() {}

    public var body: some View {
        Rectangle()
            .fill(SDColors.border.opacity(0.65))
            .frame(height: 1)
            .padding(.leading, 50)
    }
}

private struct SDSettingsIcon: View {
    let systemImage: String

    var body: some View {
        Image(systemName: systemImage)
            .font(.system(size: 14, weight: .semibold))
            .foregroundStyle(SDColors.secondary)
            .frame(width: 26, height: 26)
            .background(SDColors.secondary.opacity(0.12), in: .rect(cornerRadius: 6))
    }
}
