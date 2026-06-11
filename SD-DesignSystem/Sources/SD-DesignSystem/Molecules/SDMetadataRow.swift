import SwiftUI

public struct SDMetadataRow: View {
    let label: String
    let value: String
    var icon: String? = nil
    
    public init(label: String, value: String, icon: String? = nil) {
        self.label = label
        self.value = value
        self.icon = icon
    }

    public var body: some View {
        HStack(alignment: .top, spacing: 8) {
            if let icon {
                Image(systemName: icon)
                    .foregroundStyle(.secondary)
                    .frame(width: 16)
            }
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
                .frame(minWidth: 100, alignment: .leading)
            Text(value)
                .font(.caption)
                .foregroundStyle(.primary)
            Spacer(minLength: 0)
        }
    }
}

#Preview {
    VStack(alignment: .leading, spacing: 8) {
        SDMetadataRow(label: "Platform", value: "Android", icon: "iphone")
        SDMetadataRow(label: "Owner", value: "Android Team", icon: "person.fill")
        SDMetadataRow(label: "Last Validated", value: "Jan 15, 2026")
    }
    .padding()
}
