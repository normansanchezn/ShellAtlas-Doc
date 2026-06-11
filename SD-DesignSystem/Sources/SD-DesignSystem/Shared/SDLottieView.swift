import SwiftUI

public struct SDLottieView: View {
    private let resourceName: String
    private let loop: Bool
    private let autoplay: Bool
    private let fixedSize: CGSize

    public init(
        resourceName: String,
        loop: Bool = true,
        autoplay: Bool = true,
        fixedSize: CGSize
    ) {
        self.resourceName = resourceName
        self.loop = loop
        self.autoplay = autoplay
        self.fixedSize = fixedSize
    }

    public var body: some View {
        TimelineView(.animation(paused: !autoplay)) { timeline in
            Canvas { context, size in
                let elapsed = timeline.date.timeIntervalSinceReferenceDate
                let phase = loop ? elapsed.truncatingRemainder(dividingBy: 2.4) / 2.4 : min(elapsed / 2.4, 1)
                drawKnowledgeGraph(in: size, phase: phase, context: &context)
            }
        }
        .frame(width: fixedSize.width, height: fixedSize.height)
        .accessibilityLabel(resourceName)
    }

    private func drawKnowledgeGraph(in size: CGSize, phase: Double, context: inout GraphicsContext) {
        let center = CGPoint(x: size.width * 0.5, y: size.height * 0.48)
        let nodes = [
            CGPoint(x: size.width * 0.24, y: size.height * 0.36),
            CGPoint(x: size.width * 0.72, y: size.height * 0.30),
            CGPoint(x: size.width * 0.78, y: size.height * 0.68),
            CGPoint(x: size.width * 0.30, y: size.height * 0.72)
        ]

        let document = Path(roundedRect: CGRect(x: center.x - 22, y: center.y - 28, width: 44, height: 56), cornerRadius: 7)
        context.fill(document, with: .color(SDColors.surface))
        context.stroke(document, with: .color(SDColors.secondary.opacity(0.75)), lineWidth: 1.4)

        for index in 0..<3 {
            var line = Path()
            let y = center.y - 12 + CGFloat(index * 12)
            line.move(to: CGPoint(x: center.x - 12, y: y))
            line.addLine(to: CGPoint(x: center.x + 12, y: y))
            context.stroke(line, with: .color(SDColors.textSecondary.opacity(0.75)), lineWidth: 1)
        }

        for (index, node) in nodes.enumerated() {
            var connection = Path()
            connection.move(to: center)
            connection.addLine(to: node)
            let pulse = 0.35 + 0.45 * sin((phase * 2 * .pi) + Double(index))
            context.stroke(connection, with: .color(SDColors.secondary.opacity(pulse)), lineWidth: 1.2)

            let radius = CGFloat(6 + 2 * sin((phase * 2 * .pi) + Double(index)))
            let rect = CGRect(x: node.x - radius, y: node.y - radius, width: radius * 2, height: radius * 2)
            context.fill(Path(ellipseIn: rect), with: .color(index == 0 ? SDColors.primary : SDColors.success))
        }

        let scanX = size.width * (0.18 + 0.64 * phase)
        var scan = Path()
        scan.move(to: CGPoint(x: scanX, y: size.height * 0.22))
        scan.addLine(to: CGPoint(x: scanX, y: size.height * 0.78))
        context.stroke(scan, with: .color(SDColors.primary.opacity(0.65)), lineWidth: 1)
    }
}

public struct SDAssistantEmptyState: View {
    private let title: String
    private let message: String
    private let ctaTitle: String?
    private let action: (() -> Void)?

    public init(
        title: String = "Ask ShellDoc anything",
        message: String = "Search documentation, understand processes and discover connected knowledge across your workspace.",
        ctaTitle: String? = nil,
        action: (() -> Void)? = nil
    ) {
        self.title = title
        self.message = message
        self.ctaTitle = ctaTitle
        self.action = action
    }

    public var body: some View {
        VStack(spacing: 18) {
            SDLottieView(
                resourceName: "assistant_knowledge_graph",
                loop: true,
                autoplay: true,
                fixedSize: CGSize(width: 172, height: 138)
            )

            VStack(spacing: 8) {
                Text(title)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                Text(message)
                    .font(.callout)
                    .foregroundStyle(SDColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(3)
                    .frame(maxWidth: 480)
            }

            if let ctaTitle, let action {
                SDPrimaryButton(ctaTitle, systemImage: "arrow.down.to.line", action: action)
            }
        }
        .padding(28)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
