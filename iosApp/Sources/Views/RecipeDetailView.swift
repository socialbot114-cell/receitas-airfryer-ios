import SwiftUI
import UIKit

struct RecipeDetailView: View {
    @EnvironmentObject private var store: RecipeStore
    let recipe: Recipe
    var body: some View {
        ScrollView { VStack(alignment: .leading, spacing: 18) {
            RecipeImage(recipe: recipe).frame(height: 280)
            HStack(alignment: .top) { Text(recipe.name).font(.largeTitle.bold()); Spacer(); Button { store.toggleFavorite(recipe) } label: { Image(systemName: store.isFavorite(recipe) ? "heart.fill" : "heart").font(.title2) }.accessibilityLabel(store.isFavorite(recipe) ? "Remover dos favoritos" : "Adicionar aos favoritos") }
            Text(recipe.description).foregroundStyle(.secondary)
            ViewThatFits {
                HStack { facts }
                VStack(alignment: .leading) { facts }
            }.font(.subheadline).foregroundStyle(.secondary)
            if recipe.steps.isEmpty { ContentUnavailableView("Preparo indisponível", systemImage: "list.bullet.clipboard") }
            else { NavigationLink("Começar a cozinhar", destination: CookingModeView(recipe: recipe)).buttonStyle(.borderedProminent).controlSize(.large) }
            Text("Ingredientes").font(.title2.bold()); ForEach(recipe.ingredients, id: \.self) { Text("• \($0)") }
            Text("Modo de preparo").font(.title2.bold()); ForEach(Array(recipe.steps.enumerated()), id: \.offset) { index, step in Label(step.instruction, systemImage: "\(index + 1).circle.fill") }
        }.frame(maxWidth: 800, alignment: .leading).padding().frame(maxWidth: .infinity) }.navigationTitle("Receita").navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder private var facts: some View {
        Label("\(recipe.minutes) min", systemImage: "clock")
        Label("\(recipe.temperature)°C", systemImage: "thermometer.medium")
        Label("\(recipe.servings) porções", systemImage: "person.2")
    }
}

struct CookingModeView: View {
    let recipe: Recipe
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @State private var step = 0
    @State private var timer: CookingTimer
    @State private var isFinished = false
    @State private var showTimerCompletion = false
    private let tick = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    init(recipe: Recipe) {
        self.recipe = recipe
        _timer = State(initialValue: CookingTimer(duration: (recipe.steps.first?.minutes ?? 0) * 60))
    }

    var body: some View {
        ScrollView {
            Group {
                if isFinished { completionView }
                else { cookingView }
            }
            .padding()
            .frame(maxWidth: 760)
            .frame(maxWidth: .infinity)
        }
        .navigationTitle("Modo cozinhar")
        .onAppear { if !timer.isRunning { timer.start(at: Date()) } }
        .onReceive(tick) { date in refreshTimer(at: date) }
        .onChange(of: scenePhase) { _, phase in if phase == .active { refreshTimer(at: Date()) } }
        .alert("Tempo concluído", isPresented: $showTimerCompletion) { Button("OK", role: .cancel) {} } message: { Text("Este passo está pronto. Confira o alimento antes de continuar.") }
    }

    private var cookingView: some View {
        VStack(spacing: 28) {
            ProgressView(value: Double(step + 1), total: Double(recipe.steps.count))
                .accessibilityLabel("Progresso da receita")
                .accessibilityValue("Passo \(step + 1) de \(recipe.steps.count)")
            Text("Passo \(step + 1) de \(recipe.steps.count)").font(.headline)
            Text(recipe.steps[step].instruction).font(.title2).multilineTextAlignment(.center)
            if timer.duration > 0 {
                Text(formattedTime)
                    .font(.system(size: 72, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .frame(maxWidth: 480)
                    .padding(.vertical, 32)
                    .padding(.horizontal, 24)
                    .background(RoundedRectangle(cornerRadius: 28, style: .continuous).fill(.orange.opacity(0.12)))
                    .accessibilityLabel("Tempo restante")
                    .accessibilityValue("\(timer.remainingSeconds / 60) minutos e \(timer.remainingSeconds % 60) segundos")
                HStack {
                    if timer.remainingSeconds > 0 {
                        Button(timer.isRunning ? "Pausar" : "Continuar") { toggleTimer() }
                            .buttonStyle(.bordered)
                    }
                    Button("Reiniciar") { timer.reset(startingAt: Date()) }
                        .buttonStyle(.bordered)
                }
            }
            HStack {
                if step > 0 { Button("Anterior") { move(to: step - 1) } }
                Button(step + 1 == recipe.steps.count ? "Concluir" : "Próximo") { advance() }
                    .buttonStyle(.borderedProminent)
            }
        }
    }

    private var completionView: some View {
        VStack(spacing: 24) {
            Image(systemName: "checkmark.circle.fill").font(.system(.largeTitle, design: .rounded, weight: .bold)).foregroundStyle(.green).accessibilityHidden(true)
            Text("Receita concluída").font(.largeTitle.bold())
            Text("Bom apetite! Sirva com cuidado e confira a temperatura do alimento.").multilineTextAlignment(.center).foregroundStyle(.secondary)
            Button("Voltar à receita") { dismiss() }.buttonStyle(.borderedProminent).controlSize(.large)
        }.accessibilityElement(children: .contain)
    }

    private var formattedTime: String { "\(timer.remainingSeconds / 60):\(String(format: "%02d", timer.remainingSeconds % 60))" }

    private func move(to newStep: Int) {
        step = newStep
        timer = CookingTimer(duration: recipe.steps[newStep].minutes * 60)
        timer.start(at: Date())
    }

    private func advance() {
        guard step + 1 < recipe.steps.count else {
            timer.pause(at: Date())
            isFinished = true
            UINotificationFeedbackGenerator().notificationOccurred(.success)
            return
        }
        move(to: step + 1)
    }

    private func refreshTimer(at date: Date) {
        if timer.refresh(at: date) {
            showTimerCompletion = true
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        }
    }

    private func toggleTimer() {
        if timer.isRunning { timer.pause(at: Date()) }
        else { timer.start(at: Date()) }
    }
}
