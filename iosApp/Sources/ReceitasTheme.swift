import SwiftUI

enum ReceitasTheme {
    static let ember = Color(red: 0.76, green: 0.28, blue: 0.12)
    static let amber = Color(red: 0.91, green: 0.55, blue: 0.16)
    static let gold = Color(red: 0.66, green: 0.47, blue: 0.20)
    static let cream = Color(red: 0.97, green: 0.94, blue: 0.87)
    static let crust = Color(red: 0.28, green: 0.18, blue: 0.11)
    static let accent = ember

    static func display(_ size: CGFloat, weight: Font.Weight = .bold) -> Font {
        .system(size: size, weight: weight, design: .serif)
    }
}
