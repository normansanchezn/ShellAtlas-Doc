import SwiftUI

public struct SDBadge: View {
    let label: String
    var color: Color = .blue
    var textColor: Color = .white
    
    public init(label: String, color: Color, textColor: Color) {
        self.label = label
        self.color = color
        self.textColor = textColor
    }

    public var body: some View {
        Text(label)
            .font(.caption2.weight(.semibold))
            .foregroundStyle(textColor)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color, in: Capsule())
    }
}

#Preview {
    HStack {
        SDBadge(label: "Process", color: .blue, textColor: .red)
        SDBadge(label: "Architecture", color: .purple, textColor: .red)
        SDBadge(label: "Guide", color: .green, textColor: .red)
    }
    .padding()
}
