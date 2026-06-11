// swift-tools-version: 6.3
import PackageDescription

let package = Package(
    name: "SD-Data",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "SD-Data", targets: ["SD-Data"]),
    ],
    dependencies: [
        .package(path: "../SD-Domain"),
    ],
    targets: [
        .target(
            name: "SD-Data",
            dependencies: ["SD-Domain"]
        ),
        .testTarget(
            name: "SD-DataTests",
            dependencies: ["SD-Data"]
        ),
    ],
    swiftLanguageModes: [.v6]
)
