import SwiftUI

public struct SDMetricTile: View {
    let value: String
    let label: String
    var color: Color = .blue
    var icon: String = "doc.fill"
    
    public init(value: String, label: String, color: Color, icon: String) {
        self.value = value
        self.label = label
        self.color = color
        self.icon = icon
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Image(systemName: icon)
                    .foregroundStyle(color)
                    .font(.title3)
                Spacer()
            }
            Text(value)
                .font(.title2.weight(.bold))
                .foregroundStyle(SDColors.textPrimary)
            Text(label)
                .font(.caption)
                .foregroundStyle(SDColors.textSecondary)
                .lineLimit(2)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.regularMaterial, in: .rect(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(SDColors.border.opacity(0.8), lineWidth: 1)
        }
    }
}

public struct SDDashboardMetricsSection: View {
    let totalDocuments: Int
    let activeDocuments: Int
    let pendingReview: Int
    let possiblyOutdated: Int
    let highRisk: Int
    
    public init(totalDocuments: Int, activeDocuments: Int, pendingReview: Int, possiblyOutdated: Int, highRisk: Int) {
        self.totalDocuments = totalDocuments
        self.activeDocuments = activeDocuments
        self.pendingReview = pendingReview
        self.possiblyOutdated = possiblyOutdated
        self.highRisk = highRisk
    }

    public var body: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            SDMetricTile(value: "\(totalDocuments)", label: "Total Documents", color: SDColors.secondary, icon: "doc.fill")
            SDMetricTile(value: "\(activeDocuments)", label: "Active", color: SDColors.success, icon: "checkmark.circle.fill")
            SDMetricTile(value: "\(pendingReview)", label: "Needs Review", color: SDColors.warning, icon: "clock.fill")
            SDMetricTile(value: "\(possiblyOutdated)", label: "Possibly Outdated", color: SDColors.danger, icon: "exclamationmark.triangle.fill")
        }
    }
}

#Preview {
    SDDashboardMetricsSection(
        totalDocuments: 6,
        activeDocuments: 4,
        pendingReview: 2,
        possiblyOutdated: 2,
        highRisk: 1
    )
    .padding()
}
