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
            payload = try JSONDecoder().decode(RecipePayload.self, from: Data(contentsOf: url))
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
