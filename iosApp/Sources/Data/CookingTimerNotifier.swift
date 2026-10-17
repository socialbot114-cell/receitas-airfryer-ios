import Foundation
import UserNotifications

/// Schedules a local notification for the running step timer so the cook is
/// alerted even when the app is in the background or the screen is locked.
enum CookingTimerNotifier {
    private static let identifierPrefix = "cooking-step-timer."

    static func identifier(for recipeID: String) -> String {
        identifierPrefix + recipeID
    }

    private static var isUITesting: Bool { ProcessInfo.processInfo.arguments.contains("-ui-testing") }

    static func requestAuthorization() {
        guard !isUITesting else { return }
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { _, _ in }
    }

    static func content(recipeName: String, stepNumber: Int) -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        content.title = "Tempo concluído"
        content.body = "\(recipeName): passo \(stepNumber) pronto. Confira o alimento antes de continuar."
        content.sound = .default
        return content
    }

    static func schedule(recipeID: String, recipeName: String, stepNumber: Int, after seconds: Int) {
        cancel(recipeID: recipeID)
        guard seconds > 0, !isUITesting else { return }
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: TimeInterval(seconds), repeats: false)
        let request = UNNotificationRequest(identifier: identifier(for: recipeID), content: content(recipeName: recipeName, stepNumber: stepNumber), trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }

    static func cancel(recipeID: String) {
        let center = UNUserNotificationCenter.current()
        let requestID = identifier(for: recipeID)
        center.removePendingNotificationRequests(withIdentifiers: [requestID])
        center.removeDeliveredNotifications(withIdentifiers: [requestID])
    }
}
