import SwiftUI

@main
struct ReceitasAirfryerApp: App {
    @StateObject private var store = RecipeStore()
    var body: some Scene {
        WindowGroup { RootView().environmentObject(store) }
    }
}
