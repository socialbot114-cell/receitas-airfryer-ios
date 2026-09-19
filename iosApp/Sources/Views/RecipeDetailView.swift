import SwiftUI

struct RecipeDetailView: View {
    @EnvironmentObject private var store: RecipeStore
    let recipe: Recipe
    var body: some View {
        ScrollView { VStack(alignment: .leading, spacing: 18) {
            RecipeImage(recipe: recipe).frame(height: 230)
            HStack { Text(recipe.name).font(.largeTitle.bold()); Spacer(); Button { store.toggleFavorite(recipe) } label: { Image(systemName: store.isFavorite(recipe) ? "heart.fill" : "heart").font(.title2) } }
            Text(recipe.description).foregroundStyle(.secondary)
            HStack { Label("\(recipe.minutes) min", systemImage: "clock"); Label("\(recipe.temperature)°C", systemImage: "thermometer.medium"); Label("\(recipe.servings) porções", systemImage: "person.2") }.font(.caption).foregroundStyle(.secondary)
            NavigationLink("Começar a cozinhar", destination: CookingModeView(recipe: recipe)).buttonStyle(.borderedProminent)
            Text("Ingredientes").font(.title2.bold()); ForEach(recipe.ingredients, id: \.self) { Text("• \($0)") }
            Text("Modo de preparo").font(.title2.bold()); ForEach(Array(recipe.steps.enumerated()), id: \.offset) { index, step in Label(step.instruction, systemImage: "\(index + 1).circle.fill") }
        }.padding() }.navigationTitle("Receita").navigationBarTitleDisplayMode(.inline)
    }
}

struct CookingModeView: View {
    let recipe: Recipe
    @State private var step = 0
    @State private var remaining = 0
    private let tick = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 28) {
            ProgressView(value: Double(step + 1), total: Double(recipe.steps.count))
            Text("Passo \(step + 1) de \(recipe.steps.count)").font(.headline)
            Text(recipe.steps[step].instruction).font(.title2).multilineTextAlignment(.center)
            if remaining > 0 {
                Text("\(remaining / 60):\(String(format: "%02d", remaining % 60))")
                    .font(.system(size: 56, weight: .bold, design: .rounded).monospacedDigit())
            }
            Spacer()
            HStack {
                if step > 0 { Button("Anterior") { step -= 1; startTimerIfNeeded() } }
                Button(step + 1 == recipe.steps.count ? "Concluir" : "Próximo") { advance() }
                    .buttonStyle(.borderedProminent)
            }
        }
        .padding()
        .navigationTitle("Modo cozinhar")
        .onAppear { startTimerIfNeeded() }
        .onReceive(tick) { _ in if remaining > 0 { remaining -= 1 } }
    }

    private func startTimerIfNeeded() { remaining = max(0, recipe.steps[step].minutes * 60) }
    private func advance() { guard step + 1 < recipe.steps.count else { return }; step += 1; startTimerIfNeeded() }
}
