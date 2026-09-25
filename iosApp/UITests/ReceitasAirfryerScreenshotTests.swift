import XCTest

final class ReceitasAirfryerScreenshotTests: XCTestCase {
    func testKitchenToolsComponentScreenshot() {
        let app = XCUIApplication()
        app.launchArguments = ["-ui-testing", "-AppleLanguages", "(pt-BR)", "-AppleLocale", "pt_BR"]
        app.launch()

        let toolsHeading = app.staticTexts["Utensílios da Vovó"]
        XCTAssertTrue(toolsHeading.waitForExistence(timeout: 10))
        for _ in 0..<6 where !toolsHeading.isHittable { app.swipeUp() }
        XCTAssertTrue(toolsHeading.isHittable)
        capture(app, named: "receitas-airfryer-componentes-cozinha")
    }

    func testRecipeFlowScreenshots() {
        let app = XCUIApplication()
        app.launchArguments = ["-ui-testing", "-AppleLanguages", "(pt-BR)", "-AppleLocale", "pt_BR"]
        app.launch()

        XCTAssertTrue(app.staticTexts["Cozinhe algo gostoso hoje"].waitForExistence(timeout: 10))
        capture(app, named: "receitas-airfryer-home")

        let recipe = app.buttons["Batata crocante, 22 minutos, 200 graus"]
        XCTAssertTrue(recipe.waitForExistence(timeout: 5))
        if recipe.isHittable {
            recipe.tap()
        } else {
            recipe.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.35)).tap()
        }
        XCTAssertTrue(app.staticTexts["Batata crocante"].waitForExistence(timeout: 5))
        capture(app, named: "receitas-airfryer-recipe")

        let startCooking = app.buttons["Começar a cozinhar"]
        XCTAssertTrue(startCooking.waitForExistence(timeout: 5))
        startCooking.tap()
        XCTAssertTrue(app.staticTexts["Passo 1 de 5"].waitForExistence(timeout: 5))
        app.buttons["Próximo"].tap()
        app.buttons["Próximo"].tap()
        XCTAssertTrue(app.staticTexts["Tempo restante"].waitForExistence(timeout: 5))
        capture(app, named: "receitas-airfryer-timer")
    }

    private func capture(_ app: XCUIApplication, named name: String) {
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
