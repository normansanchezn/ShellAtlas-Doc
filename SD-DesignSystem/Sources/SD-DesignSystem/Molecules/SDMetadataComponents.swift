import SwiftUI

public struct SDMetadataPanel<Content: View>: View {
    private let title: String
    private let content: Content

    public init(_ title: String = "Attributes", @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.caption.weight(.bold))
                .foregroundStyle(SDColors.textMuted)
                .textCase(.uppercase)

            content
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SDColors.cardBackground.opacity(0.72), in: .rect(cornerRadius: 8))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(SDColors.border.opacity(0.72), lineWidth: 1)
        }
    }
}

public struct SDMetadataGrid<Content: View>: View {
    private let minimumColumnWidth: CGFloat
    private let content: Content

    public init(minimumColumnWidth: CGFloat = 160, @ViewBuilder content: () -> Content) {
        self.minimumColumnWidth = minimumColumnWidth
        self.content = content()
    }

    public var body: some View {
        LazyVGrid(
            columns: [GridItem(.adaptive(minimum: minimumColumnWidth), spacing: 14, alignment: .leading)],
            alignment: .leading,
            spacing: 14
        ) {
            content
        }
    }
}

public struct SDMetadataItem: View {
    private let label: String
    private let value: String
    private let systemImage: String?

    public init(label: String, value: String, systemImage: String? = nil) {
        self.label = label
        self.value = value
        self.systemImage = systemImage
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                if let systemImage {
                    Image(systemName: systemImage)
                        .font(.caption)
                }
                Text(label)
            }
            .font(.caption.weight(.semibold))
            .foregroundStyle(SDColors.textMuted)

            Text(value.isEmpty ? "Not set" : value)
                .font(.callout.weight(.semibold))
                .foregroundStyle(value.isEmpty ? SDColors.textMuted : SDColors.textPrimary)
                .lineLimit(3)
                .textSelection(.enabled)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

public struct SDMetadataTagGroup: View {
    private let tags: [String]

    public init(tags: [String]) {
        self.tags = tags
    }

    public var body: some View {
        if tags.isEmpty {
            Text("No tags")
                .font(.callout)
                .foregroundStyle(SDColors.textMuted)
        } else {
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 92, maximum: 176), spacing: 6, alignment: .leading)],
                alignment: .leading,
                spacing: 6
            ) {
                ForEach(tags, id: \.self) { tag in
                    SDTagView(tag)
                }
            }
        }
    }
}
