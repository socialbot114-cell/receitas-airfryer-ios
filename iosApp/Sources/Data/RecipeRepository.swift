import Foundation
import Combine

struct RecipeRepository {
    let payload: RecipePayload
    init(bundle: Bundle = .main) {
        guard let url = bundle.url(forResource: "recipes", withExtension: "json", subdirectory: "Recipes"),
              let data = try? Data(contentsOf: url), let decoded = try? JSONDecoder().decode(RecipePayload.self, from: data) else {
            payload = RecipePayload(recipes: [], guide: [])
            return
        }
        payload = decoded
    }
    func search(_ query: String, category: String? = nil, quickOnly: Bool = false, healthyOnly: Bool = false) -> [Recipe] {
        let needle = query.normalized
        return payload.recipes.filter { recipe in
            let text = ([recipe.name, recipe.category, recipe.mainIngredient] + recipe.ingredients).joined(separator: " ").normalized
            return (needle.isEmpty || text.contains(needle)) && (category == nil || recipe.category == category) && (!quickOnly || recipe.minutes <= 15) && (!healthyOnly || recipe.healthy)
        }
    }
}

@MainActor final class RecipeStore: ObservableObject {
    @Published private(set) var favorites: Set<String>
    let repository: RecipeRepository
    private let defaults: UserDefaults
    init(defaults: UserDefaults = .standard, repository: RecipeRepository = RecipeRepository()) {
        self.defaults = defaults; self.repository = repository
        favorites = Set(defaults.stringArray(forKey: "favoriteRecipeIDs") ?? [])
    }
    func toggleFavorite(_ recipe: Recipe) {
        if favorites.contains(recipe.id) { favorites.remove(recipe.id) } else { favorites.insert(recipe.id) }
        defaults.set(Array(favorites).sorted(), forKey: "favoriteRecipeIDs")
    }
    func isFavorite(_ recipe: Recipe) -> Bool { favorites.contains(recipe.id) }
}
