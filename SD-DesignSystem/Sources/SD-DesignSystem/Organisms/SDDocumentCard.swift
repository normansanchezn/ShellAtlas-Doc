import SwiftUI
import SD_Domain
import DS_Core

public struct SDDocumentCard: View {
    let document: KnowledgeDocument
    var healthResult: DocumentHealthResult? = nil
    
    public init(document: KnowledgeDocument, healthResult: DocumentHealthResult? = nil) {
        self.document = document
        self.healthResult = healthResult
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(document.title)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(SDColors.textPrimary)
                        .lineLimit(2)
                    Text(document.area)
                        .font(.caption)
                        .foregroundStyle(SDColors.textSecondary)
                }
                Spacer()
                if let health = healthResult {
                    SDScorePill(score: health.healthScore)
                }
            }

            HStack(spacing: 6) {
                SDBadge(label: document.type.displayName, color: typeColor, textColor: .primary)
                SDStatusChip(status: document.status)
                SDBadge(label: document.platform.displayName, color: SDColors.border, textColor: SDColors.textPrimary)
            }

            HStack(spacing: 16) {
                Label(document.owner, systemImage: "person.fill")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
                Spacer()
                Label(document.confidence.displayName, systemImage: "chart.bar.fill")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            }

            if !document.tags.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 4) {
                        ForEach(document.tags.prefix(5), id: \.self) { tag in
                            Text("#\(tag)")
                                .font(.caption2)
                                .foregroundStyle(SDColors.textSecondary)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(SDColors.border.opacity(0.7), in: Capsule())
                        }
                    }
                }
            }
        }
        .padding(12)
        .background(.regularMaterial, in: .rect(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(SDColors.border.opacity(0.8), lineWidth: 1)
        }
    }

    private var typeColor: Color {
        switch document.type {
        case .process: SDColors.secondary.opacity(0.22)
        case .architecture: SDColors.primary.opacity(0.22)
        case .guide: SDColors.success.opacity(0.22)
        case .runbook: SDColors.warning.opacity(0.22)
        case .decision: SDColors.danger.opacity(0.22)
        case .reference: SDColors.secondary.opacity(0.16)
        }
    }
}
