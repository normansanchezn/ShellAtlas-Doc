import SwiftUI

public enum SDColors {
    public static let background = Color(hex: "0B1220")
    public static let appBackground = background
    public static let sidebarBackground = Color(hex: "111827")
    public static let topBarBackground = Color(hex: "171923")
    public static let contentBackground = background
    public static let surface = Color(hex: "1E293B")
    public static let cardBackground = surface
    public static let elevatedSurface = Color(hex: "273449")
    public static let primary = Color(hex: "F6C343")
    public static let secondary = Color(hex: "38BDF8")
    public static let success = Color(hex: "10B981")
    public static let textPrimary = Color(hex: "F8FAFC")
    public static let textSecondary = Color(hex: "CBD5E1")
    public static let textMuted = Color(hex: "94A3B8")
    public static let border = Color(hex: "334155")
    public static let warning = Color(hex: "F59E0B")
    public static let danger = Color(hex: "EF4444")

    public static let accentSoft = primary.opacity(0.16)
    public static let warningSoft = warning.opacity(0.14)
    public static let successSoft = success.opacity(0.14)
    public static let dangerSoft = danger.opacity(0.14)

    public static let shellYellow = primary
    public static let shellOrange = warning
    public static let shellRed = danger

    public static let healthGreen = success
    public static let healthOrange = warning
    public static let healthRed = danger

    public static let accent = primary
}

public extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8)  & 0xFF) / 255
        let b = Double(int & 0xFF)          / 255
        self.init(red: r, green: g, blue: b)
    }
}
