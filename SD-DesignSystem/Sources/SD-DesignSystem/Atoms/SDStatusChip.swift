import SwiftUI
import SD_Domain

public struct SDStatusChip: View {
    let status: DocumentStatus
    
    public init(status: DocumentStatus) {
        self.status = status
    }

    public var body: some View {
        SDBadge(label: status.displayName, color: color.opacity(0.18), textColor: color)
    }

    private var color: Color {
        switch status {
        case .active: SDColors.success
        case .review: SDColors.warning
        case .outdated: SDColors.danger
        case .archived: SDColors.textSecondary
        case .draft: SDColors.secondary
        }
    }
}

#Preview {
    HStack {
        SDStatusChip(status: .active)
        SDStatusChip(status: .review)
        SDStatusChip(status: .outdated)
        SDStatusChip(status: .archived)
    }
    .padding()
}
