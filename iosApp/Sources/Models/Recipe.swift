import Foundation

struct CookStep: Codable, Hashable { let instruction: String; let minutes: Int }
struct Recipe: Codable, Identifiable, Hashable {
    let id: String; let name: String; let description: String; let category: String
    let mainIngredient: String; let minutes: Int; let temperature: Int; let servings: Int
    let calories: Int; let difficulty: String; let preheat: Bool; let turn: Bool
    let ingredients: [String]; let pantryKeys: [String]; let steps: [CookStep]
    let healthy: Bool; let imageName: String?; let dietaryTags: [String]; let allergens: [String]
}
struct GuideItem: Codable, Hashable { let name: String; let cut: String; let temperature: Int; let time: String; let tip: String }
struct RecipePayload: Codable { let recipes: [Recipe]; let guide: [GuideItem] }

extension String {
    var normalized: String { folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current).trimmingCharacters(in: .whitespacesAndNewlines) }
}
