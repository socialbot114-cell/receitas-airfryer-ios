import Foundation

struct CookStep: Codable, Hashable { let instruction: String; let minutes: Int }
struct Recipe: Codable, Identifiable, Hashable {
    let id: String; let name: String; let description: String; let category: String
    let mainIngredient: String; let minutes: Int; let temperature: Int; let servings: Int
    let calories: Int; let difficulty: String; let preheat: Bool; let turn: Bool
    let ingredients: [String]; let pantryKeys: [String]; let steps: [CookStep]
    let healthy: Bool; let imageName: String?
}
struct GuideItem: Codable, Hashable { let name: String; let cut: String; let temperature: Int; let time: String; let tip: String }
struct RecipePayload: Codable { let recipes: [Recipe]; let guide: [GuideItem] }

extension String {
    var normalized: String { folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current).trimmingCharacters(in: .whitespacesAndNewlines) }
}

struct CookingTimer: Equatable {
    let duration: Int
    private(set) var remainingSeconds: Int
    private(set) var endDate: Date?

    init(duration: Int) {
        self.duration = max(0, duration)
        remainingSeconds = max(0, duration)
    }

    var isRunning: Bool { endDate != nil && remainingSeconds > 0 }

    func remaining(at date: Date) -> Int {
        guard let endDate else { return remainingSeconds }
        return max(0, Int(ceil(endDate.timeIntervalSince(date))))
    }

    mutating func start(at date: Date) {
        guard remainingSeconds > 0 else { return }
        endDate = date.addingTimeInterval(TimeInterval(remainingSeconds))
    }

    mutating func pause(at date: Date) {
        remainingSeconds = remaining(at: date)
        endDate = nil
    }

    mutating func reset(startingAt date: Date? = nil) {
        remainingSeconds = duration
        endDate = date.map { $0.addingTimeInterval(TimeInterval(duration)) }
    }

    @discardableResult
    mutating func refresh(at date: Date) -> Bool {
        guard endDate != nil else { return false }
        remainingSeconds = remaining(at: date)
        guard remainingSeconds == 0 else { return false }
        endDate = nil
        return true
    }
}
