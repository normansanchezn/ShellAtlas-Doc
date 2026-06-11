import SwiftUI
import DS_Core

public struct SDHealthPanel: View {
    let result: DocumentHealthResult
    
    public init(result: DocumentHealthResult) {
        self.result = result
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SDHealthScoreRow(result: result)

            if !result.reasons.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Reasons")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.textSecondary)
                    ForEach(result.reasons, id: \.self) { reason in
                        HStack(alignment: .top, spacing: 6) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundStyle(SDColors.warning)
                                .font(.caption)
                                .frame(width: 14)
                            Text(reason)
                                .font(.caption)
                                .foregroundStyle(SDColors.textPrimary)
                        }
                    }
                }
            }

            if !result.suggestedActions.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Suggested Actions")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(SDColors.textSecondary)
                    ForEach(result.suggestedActions, id: \.self) { action in
                        HStack(alignment: .top, spacing: 6) {
                            Image(systemName: "arrow.right.circle.fill")
                                .foregroundStyle(SDColors.secondary)
                                .font(.caption)
                                .frame(width: 14)
                            Text(action)
                                .font(.caption)
                                .foregroundStyle(SDColors.textPrimary)
                        }
                    }
                }
            }
        }
        .padding()
        .background(.regularMaterial, in: .rect(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(SDColors.border.opacity(0.8), lineWidth: 1)
        }
    }
}
