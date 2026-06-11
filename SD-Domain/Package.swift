// swift-tools-version: 6.3
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "SD-Domain",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "SD-Domain",
            targets: ["SD-Domain"]
        ),
    ],
    dependencies: [
        .package(path: "../DS-Core"),
    ],
    targets: [
        .target(
            name: "SD-Domain",
            dependencies: ["DS-Core"]
        ),
        .testTarget(
            name: "SD-DomainTests",
            dependencies: ["SD-Domain"]
        ),
    ],
    swiftLanguageModes: [.v6]
)
