import XCTest
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

    func testRecipeContractHasThirtyOneCatalogItemsInBundledPayload() throws {
        let url = try XCTUnwrap(RecipeRepository.catalogURL(in: Bundle(for: Self.self)))
        let payload = try JSONDecoder().decode(RecipePayload.self, from: Data(contentsOf: url))
        XCTAssertEqual(payload.recipes.count, 31)
        XCTAssertEqual(Set(payload.recipes.map(\.id)).count, 31)
    }

    func testRepositoryReportsSuccessfulBundledLoad() {
        let repository = RecipeRepository(bundle: Bundle(for: Self.self))
        XCTAssertNil(repository.loadError)
        XCTAssertEqual(repository.payload.recipes.count, 31)
    }

    func testImageCanBeLoadedFromBundledImagesDirectory() {
        XCTAssertNotNil(RecipeBundleImage.load(name: "asset_batata_crocante", bundle: Bundle(for: Self.self)))
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
