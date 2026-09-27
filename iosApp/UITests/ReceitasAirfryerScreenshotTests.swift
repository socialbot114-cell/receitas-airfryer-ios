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

        let discoverTab = app.tabBars.buttons["Descobrir"]
        XCTAssertTrue(discoverTab.waitForExistence(timeout: 5))
        discoverTab.tap()

        let searchField = app.textFields["Buscar receita ou ingrediente"]
        XCTAssertTrue(searchField.waitForExistence(timeout: 5))
        searchField.tap()
        searchField.typeText("falafel\n")

        let recipe = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] %@", "Falafel na Air Fryer")).firstMatch
        XCTAssertTrue(recipe.waitForExistence(timeout: 5))
        for _ in 0..<5 where !recipe.isHittable { app.swipeUp() }
        XCTAssertTrue(recipe.isHittable)
        capture(app, named: "receitas-airfryer-nova-receita-falafel")
        recipe.tap()
        XCTAssertTrue(app.staticTexts["Falafel na Air Fryer"].waitForExistence(timeout: 5))
        capture(app, named: "receitas-airfryer-receita-falafel")

        let startCooking = app.buttons["Começar a cozinhar"]
        XCTAssertTrue(startCooking.waitForExistence(timeout: 5))
        startCooking.tap()
        XCTAssertTrue(app.staticTexts["Passo 1 de 6"].waitForExistence(timeout: 5))
        app.buttons["Próximo"].tap()
        app.buttons["Próximo"].tap()
        app.buttons["Próximo"].tap()
        XCTAssertTrue(app.staticTexts["Tempo restante"].waitForExistence(timeout: 5))
        capture(app, named: "receitas-airfryer-timer-falafel")
    }

    private func capture(_ app: XCUIApplication, named name: String) {
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
