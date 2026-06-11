import SwiftUI

public struct SDEmptyStateView: View {
    private let title: String
    private let systemImage: String
    private let message: String

    public init(_ title: String, systemImage: String, message: String) {
        self.title = title
        self.systemImage = systemImage
        self.message = message
    }

    public var body: some View {
        ContentUnavailableView(title, systemImage: systemImage, description: Text(message))
            .foregroundStyle(SDColors.textPrimary)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(.regularMaterial)
    }
}

public struct SDLoadingView: View {
    private let title: String

    public init(_ title: String = "Loading...") {
        self.title = title
    }

    public var body: some View {
        ProgressView(title)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

public struct SDLottieLoadingView: View {
    private let title: String
    private let message: String?
    private let size: CGSize

    public init(
        _ title: String = "Loading...",
        message: String? = nil,
        size: CGSize = CGSize(width: 132, height: 104)
    ) {
        self.title = title
        self.message = message
        self.size = size
    }

    public var body: some View {
        VStack(spacing: 14) {
            SDLottieView(
                resourceName: "assistant_knowledge_graph",
                loop: true,
                autoplay: true,
                fixedSize: size
            )

            VStack(spacing: 5) {
                Text(title)
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                if let message {
                    Text(message)
                        .font(.callout)
                        .foregroundStyle(SDColors.textSecondary)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: 360)
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SDColors.contentBackground)
        .accessibilityElement(children: .combine)
    }
}

public struct SDErrorStateView: View {
    private let title: String
    private let message: String
    private let retry: (() -> Void)?

    public init(_ title: String = "Something went wrong", message: String, retry: (() -> Void)? = nil) {
        self.title = title
        self.message = message
        self.retry = retry
    }

    public var body: some View {
        VStack(spacing: 12) {
            ContentUnavailableView(title, systemImage: "exclamationmark.triangle", description: Text(message))
            if let retry {
                Button("Retry", action: retry)
                    .buttonStyle(.borderedProminent)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
