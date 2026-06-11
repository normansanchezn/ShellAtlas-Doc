import SwiftUI
import DS_Core

struct SDHealthScoreRow: View {
    let result: DocumentHealthResult

    var body: some View {
        HStack(spacing: 12) {
            SDScorePill(score: result.healthScore)
            VStack(alignment: .leading, spacing: 2) {
                Text(result.recommendation.displayName)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(scoreColor)
                if let firstReason = result.reasons.first {
                    Text(firstReason)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
            Spacer()
        }
    }

    private var scoreColor: Color {
        switch result.recommendation {
        case .upToDate: .green
        case .reviewRecommended: .orange
        case .reviewRequired: .red
        case .criticalReview: .red.mix(with: .black, by: 0.2)
        }
    }
}
