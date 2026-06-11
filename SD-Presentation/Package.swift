// swift-tools-version: 6.3
import PackageDescription

let package = Package(
    name: "SD-Presentation",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "SD-Presentation", targets: ["SD-Presentation"]),
    ],
    dependencies: [
        .package(path: "../SD-Domain"),
        .package(path: "../SD-DesignSystem"),
        .package(path: "../DS-Core")
    ],
    targets: [
        .target(
            name: "SD-Presentation",
            dependencies: ["SD-Domain", "SD-DesignSystem", "DS-Core"]
        ),
        .testTarget(
            name: "SD-PresentationTests",
            dependencies: ["SD-Presentation"]
        ),
    ],
    swiftLanguageModes: [.v6]
)
