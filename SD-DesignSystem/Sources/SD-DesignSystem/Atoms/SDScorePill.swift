import SwiftUI

public struct SDScorePill: View {
    let score: Int

    public init(score: Int) {
        self.score = score
    }
    
    public var body: some View {
        HStack(spacing: 4) {
            Circle()
                .fill(scoreColor)
                .frame(width: 8, height: 8)
            Text("\(score)")
                .font(.caption.weight(.bold))
                .foregroundStyle(scoreColor)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(scoreColor.opacity(0.12), in: Capsule())
    }

    var scoreColor: Color {
        switch score {
        case 80...100: .green
        case 60..<80: .orange
        case 40..<60: .red
        default: .red.mix(with: .black, by: 0.2)
        }
    }
}

#Preview {
    HStack {
        SDScorePill(score: 95)
        SDScorePill(score: 72)
        SDScorePill(score: 50)
        SDScorePill(score: 25)
    }
    .padding()
}
