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
        let url = try XCTUnwrap(Bundle(for: Self.self).url(forResource: "recipes", withExtension: "json"))
        let payload = try JSONDecoder().decode(RecipePayload.self, from: Data(contentsOf: url))
        XCTAssertEqual(payload.recipes.count, 31)
        XCTAssertEqual(Set(payload.recipes.map(\.id).count), 31)
    }
}
