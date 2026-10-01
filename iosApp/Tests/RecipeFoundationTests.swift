import XCTest
import UserNotifications
@testable import ReceitasAirfryer

final class RecipeFoundationTests: XCTestCase {
    func testNormalizationIgnoresAccentsAndCase() {
        XCTAssertEqual("Limão com Açaí".normalized, "limao com acai")
    }

    func testCookStepRoundTrips() throws {
        let step = CookStep(instruction: "Agite o cesto", minutes: 4)
        let data = try JSONEncoder().encode(step)
        XCTAssertEqual(try JSONDecoder().decode(CookStep.self, from: data), step)
    }

    func testRecipeContractHasThreeHundredCatalogItemsInBundledPayload() throws {
        let url = try XCTUnwrap(RecipeRepository.catalogURL(in: Bundle(for: Self.self)))
        let payload = try JSONDecoder().decode(RecipePayload.self, from: Data(contentsOf: url))
        var seenIDs = Set<String>()
        let uniqueRecipes = payload.recipes.filter { seenIDs.insert($0.id).inserted }
        XCTAssertEqual(uniqueRecipes.count, 300)
        XCTAssertEqual(Set(uniqueRecipes.map(\.id)).count, 300)
        XCTAssertEqual(Set(uniqueRecipes.map { $0.name.normalized }).count, 300)
    }

    func testRepositoryReportsSuccessfulBundledLoad() {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        XCTAssertNil(repository.loadError)
        XCTAssertEqual(repository.payload.recipes.count, 300)
    }

    func testImageCanBeLoadedFromBundledImagesDirectory() {
        XCTAssertNotNil(RecipeBundleImage.load(name: "asset_batata_crocante", bundle: Bundle(for: Self.self)))
    }

    func testAllProvidedComponentArtworkIsBundled() {
        let bundle = Bundle(for: Self.self)
        for assetName in ComponentArtwork.allAssetNames {
            XCTAssertNotNil(RecipeBundleImage.load(name: assetName, bundle: bundle), "Missing component artwork: \(assetName)")
        }
    }

    func testEveryRecipeResolvesToBundledComponentArtwork() throws {
        let bundle = Bundle(for: Self.self)
        let repository = RecipeRepository(bundle: bundle)
        for recipe in repository.payload.recipes {
            let assetName = ComponentArtwork.recipeImageName(for: recipe)
            XCTAssertNotNil(RecipeBundleImage.load(name: assetName, bundle: bundle), "Missing artwork for \(recipe.id): \(assetName)")
        }
    }

    func testRecipeSpecificArtworkPrecedesCategoryFallback() throws {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        let salmon = try XCTUnwrap(repository.payload.recipes.first { $0.id == "salmao-ervas" })
        let falafel = try XCTUnwrap(repository.payload.recipes.first { $0.id == "falafel-airfryer" })
        let potatoes = try XCTUnwrap(repository.payload.recipes.first { $0.id == "batata-crocante" })

        XCTAssertEqual(ComponentArtwork.recipeImageName(for: salmon), "asset_salmao_ervas")
        XCTAssertEqual(ComponentArtwork.recipeImageName(for: falafel), "component_dish_legumes")
        XCTAssertEqual(ComponentArtwork.recipeImageName(for: potatoes), "component_dish_batata_crocante")
    }

    func testDailyHighlightsAreUniqueStableAndRotate() {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "America/Sao_Paulo")!
        let today = Date(timeIntervalSince1970: 1_790_000_000)
        let tomorrow = today.addingTimeInterval(86_400)

        let highlights = repository.dailyHighlights(on: today, calendar: calendar)
        XCTAssertEqual(highlights.count, 6)
        XCTAssertEqual(Set(highlights.map(\.id)).count, 6)
        XCTAssertEqual(highlights.map(\.id), repository.dailyHighlights(on: today.addingTimeInterval(60), calendar: calendar).map(\.id))
        XCTAssertNotEqual(highlights.map(\.id), repository.dailyHighlights(on: tomorrow, calendar: calendar).map(\.id))
    }

    func testCookingGuideIsBundled() {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        XCTAssertEqual(repository.payload.guide.count, 15)
        XCTAssertTrue(repository.payload.guide.allSatisfy { !$0.name.isEmpty && $0.temperature > 0 })
    }

    func testTimerNotificationNamesRecipeAndStep() {
        let content = CookingTimerNotifier.content(recipeName: "Batata crocante", stepNumber: 2)
        XCTAssertEqual(content.title, "Tempo concluído")
        XCTAssertTrue(content.body.contains("Batata crocante"))
        XCTAssertTrue(content.body.contains("passo 2"))
    }

    func testVegetableRecipeIncludesMatchingCarrotArtwork() throws {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        let recipe = try XCTUnwrap(repository.payload.recipes.first { $0.id == "legumes-mediterraneos" })
        XCTAssertTrue(ComponentArtwork.ingredientMatches(recipe.ingredients).contains { $0.id == "cenoura" })
    }

    func testSearchNormalizesAccentsAndCombinesFilters() throws {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        let fishRecipes = repository.search("LIMÃO", category: "Peixes")
        XCTAssertTrue(fishRecipes.contains { $0.id == "salmao-ervas" })
        XCTAssertTrue(fishRecipes.contains { $0.id == "tilapia-limao" })

        let quickRecipes = repository.search("", quickOnly: true)
        XCTAssertFalse(quickRecipes.isEmpty)
        XCTAssertTrue(quickRecipes.allSatisfy { $0.minutes <= 15 })

        let lighterRecipes = repository.search("", healthyOnly: true)
        XCTAssertFalse(lighterRecipes.isEmpty)
        XCTAssertTrue(lighterRecipes.allSatisfy(\.healthy))
    }

    @MainActor
    func testFavoritesPersistAcrossStoreInstances() throws {
        let suiteName = "RecipeFoundationTests.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suiteName))
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        let recipe = try XCTUnwrap(repository.payload.recipes.first)
        let store = RecipeStore(defaults: defaults, repository: repository)
        store.toggleFavorite(recipe)
        XCTAssertTrue(store.isFavorite(recipe))

        let restoredStore = RecipeStore(defaults: defaults, repository: repository)
        XCTAssertTrue(restoredStore.isFavorite(recipe))
    }

    func testTimerUsesDeadlineAcrossLongUpdateGap() {
        let start = Date(timeIntervalSince1970: 1_000)
        var timer = CookingTimer(duration: 120)
        timer.start(at: start)

        XCTAssertEqual(timer.remaining(at: start.addingTimeInterval(90)), 30)
        XCTAssertTrue(timer.refresh(at: start.addingTimeInterval(121)))
        XCTAssertEqual(timer.remainingSeconds, 0)
        XCTAssertFalse(timer.isRunning)
    }

    func testTimerPauseResumeAndReset() {
        let start = Date(timeIntervalSince1970: 1_000)
        var timer = CookingTimer(duration: 120)
        timer.start(at: start)
        timer.pause(at: start.addingTimeInterval(30))
        XCTAssertEqual(timer.remaining(at: start.addingTimeInterval(300)), 90)

        timer.start(at: start.addingTimeInterval(300))
        XCTAssertEqual(timer.remaining(at: start.addingTimeInterval(330)), 60)
        timer.reset()
        XCTAssertEqual(timer.remainingSeconds, 120)
        XCTAssertFalse(timer.isRunning)
    }
}
