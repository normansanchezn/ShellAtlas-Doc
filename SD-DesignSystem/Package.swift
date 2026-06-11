// swift-tools-version: 6.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "SD-DesignSystem",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        // Products define the executables and libraries a package produces, making them visible to other packages.
        .library(
            name: "SD-DesignSystem",
            targets: ["SD-DesignSystem"],
        ),
    ],
    dependencies: [
        .package(path: "../DS-Core"),
        .package(path: "../SD-Domain")
    ],
    targets: [
        .target(
            name: "SD-DesignSystem",
            dependencies: ["DS-Core", "SD-Domain"],
            resources: [
                .process("Resources")
            ]
        ),
        .testTarget(
            name: "SD-DesignSystemTests",
            dependencies: ["SD-DesignSystem"]
        ),
    ],
    swiftLanguageModes: [.v6]
)
