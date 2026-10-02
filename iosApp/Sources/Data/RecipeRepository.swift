import Foundation
import Combine

struct RecipeRepository {
    let payload: RecipePayload
    let loadError: String?

    init(bundle: Bundle = .main) {
        guard let url = Self.catalogURL(in: bundle) else {
            payload = RecipePayload(recipes: [], guide: [])
            loadError = "O catálogo de receitas não foi encontrado."
            return
        }
        do {
            let decoded = try JSONDecoder().decode(RecipePayload.self, from: Data(contentsOf: url))
            var seenIDs = Set<String>()
            let uniqueRecipes = decoded.recipes.filter { seenIDs.insert($0.id).inserted }
            payload = RecipePayload(recipes: uniqueRecipes, guide: decoded.guide)
            loadError = nil
        } catch {
            payload = RecipePayload(recipes: [], guide: [])
            loadError = "Não foi possível abrir o catálogo de receitas."
        }
    }
    static func catalogURL(in bundle: Bundle) -> URL? {
        bundle.url(forResource: "recipes", withExtension: "json", subdirectory: "Recipes")
            ?? bundle.url(forResource: "recipes", withExtension: "json")
    }
    func search(_ query: String, category: String? = nil, quickOnly: Bool = false, healthyOnly: Bool = false) -> [Recipe] {
        let needle = query.normalized
        return payload.recipes.filter { recipe in
            let text = ([recipe.name, recipe.category, recipe.mainIngredient] + recipe.ingredients).joined(separator: " ").normalized
            return (needle.isEmpty || text.contains(needle)) && (category == nil || recipe.category == category) && (!quickOnly || recipe.minutes <= 15) && (!healthyOnly || recipe.healthy)
        }
    }

    /// Picks recipes spread across the catalog that change once per day, so the
    /// home screen surfaces the whole collection over time.
    func dailyHighlights(on date: Date = Date(), count: Int = 6, calendar: Calendar = .current, where isEligible: (Recipe) -> Bool = { _ in true }) -> [Recipe] {
        let recipes = payload.recipes.filter(isEligible)
        let total = min(count, recipes.count)
        guard total > 0 else { return [] }
        let day = calendar.ordinality(of: .day, in: .era, for: date) ?? 0
        let stride = recipes.count / total
        return (0..<total).map { recipes[(day + $0 * stride) % recipes.count] }
    }
}

enum RecipeCatalogState: Equatable {
    case loaded
    case empty
    case failed(String)
}

@MainActor final class RecipeStore: ObservableObject {
    @Published private(set) var favorites: Set<String>
    let repository: RecipeRepository
    private let defaults: UserDefaults
    init(defaults: UserDefaults = .standard, repository: RecipeRepository = RecipeRepository()) {
        self.defaults = defaults; self.repository = repository
        favorites = Set(defaults.stringArray(forKey: "favoriteRecipeIDs") ?? [])
    }
    var catalogState: RecipeCatalogState {
        if let error = repository.loadError { return .failed(error) }
        return repository.payload.recipes.isEmpty ? .empty : .loaded
    }
    func toggleFavorite(_ recipe: Recipe) {
        if favorites.contains(recipe.id) { favorites.remove(recipe.id) } else { favorites.insert(recipe.id) }
        defaults.set(Array(favorites).sorted(), forKey: "favoriteRecipeIDs")
    }
    func isFavorite(_ recipe: Recipe) -> Bool { favorites.contains(recipe.id) }
}
