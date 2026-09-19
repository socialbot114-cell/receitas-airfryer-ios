import XCTest

final class ReceitasAirfryerScreenshotTests: XCTestCase {
    func testHomeScreenshot() {
        let app = XCUIApplication()
        app.launchArguments = ["-ui-testing", "-AppleLanguages", "(pt-BR)", "-AppleLocale", "pt_BR"]
        app.launch()

        XCTAssertTrue(app.staticTexts["Cozinhe algo gostoso hoje"].waitForExistence(timeout: 10))
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = "receitas-airfryer-home"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
