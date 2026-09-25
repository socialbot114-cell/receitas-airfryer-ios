import SwiftUI
import UIKit

struct RecipeDetailView: View {
    @EnvironmentObject private var store: RecipeStore
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    let recipe: Recipe

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                if horizontalSizeClass == .regular {
                    HStack(alignment: .center, spacing: 30) {
                        RecipeImage(recipe: recipe, height: 380)
                            .frame(width: 380, height: 380)
                        recipeIntro
                    }
                    .padding(24)
                    .background(.white, in: RoundedRectangle(cornerRadius: 28, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 28, style: .continuous).stroke(ReceitasTheme.crust.opacity(0.07), lineWidth: 1))
                } else {
                    VStack(alignment: .leading, spacing: 20) {
                        RecipeImage(recipe: recipe, height: 248)
                        recipeIntro
                    }
                }

                VStack(alignment: .leading, spacing: 12) {
                    sectionTitle("Ingredientes", artwork: ComponentArtwork.ribbon)
                    ForEach(recipe.ingredients, id: \.self) { ingredient in IngredientLine(text: ingredient) }
                }
                .padding(18)
                .background(.white, in: RoundedRectangle(cornerRadius: 22, style: .continuous))

                VStack(alignment: .leading, spacing: 12) {
                    sectionTitle("Modo de preparo", artwork: ComponentArtwork.rosemary)
                    ForEach(Array(recipe.steps.enumerated()), id: \.offset) { index, step in
                        Label(step.instruction, systemImage: "\(index + 1).circle.fill")
                            .labelStyle(NumberedStepLabelStyle())
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(18)
                .background(.white, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
            }
            .frame(maxWidth: horizontalSizeClass == .regular ? 1_000 : .infinity, alignment: .leading)
            .padding(horizontalSizeClass == .regular ? 28 : 16)
            .padding(.vertical, 18)
            .frame(maxWidth: .infinity)
        }
        .background(ReceitasTheme.cream.opacity(0.48))
        .navigationTitle("Receita")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var recipeIntro: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 7) {
                    Text(recipe.name)
                        .font(ReceitasTheme.display(horizontalSizeClass == .regular ? 34 : 29, weight: .bold))
                        .foregroundStyle(ReceitasTheme.crust)
                    Text(recipe.description)
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
                Spacer(minLength: 4)
                Button { store.toggleFavorite(recipe) } label: {
                    ComponentArtworkView(name: store.isFavorite(recipe) ? ComponentArtwork.heart : ComponentArtwork.woodenHeart)
                        .frame(width: 46, height: 46)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(store.isFavorite(recipe) ? "Remover dos favoritos" : "Adicionar aos favoritos")
            }

            ViewThatFits(in: .horizontal) {
                HStack(spacing: 10) { recipeFacts }
                VStack(alignment: .leading, spacing: 8) { recipeFacts }
            }

            if !recipe.steps.isEmpty {
                NavigationLink(destination: CookingModeView(recipe: recipe)) {
                    Label("Começar a cozinhar", systemImage: "play.fill")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 7)
                }
                .buttonStyle(.borderedProminent)
                .tint(ReceitasTheme.ember)
                .controlSize(.large)
            } else {
                ContentUnavailableView("Preparo indisponível", systemImage: "list.bullet.clipboard")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder private var recipeFacts: some View {
        factPill("\(recipe.minutes) min", systemImage: "clock")
        factPill("\(recipe.temperature)°C", systemImage: "thermometer.medium")
        factPill("\(recipe.servings) porções", systemImage: "person.2")
    }

    private func factPill(_ title: String, systemImage: String) -> some View {
        Label(title, systemImage: systemImage)
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(ReceitasTheme.crust.opacity(0.76))
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(ReceitasTheme.cream, in: Capsule())
    }

    private func sectionTitle(_ title: String, artwork: String) -> some View {
        HStack(spacing: 8) {
            ComponentArtworkView(name: artwork).frame(width: 30, height: 30)
            Text(title).font(ReceitasTheme.display(22, weight: .bold)).foregroundStyle(ReceitasTheme.crust)
        }
    }
}

private struct IngredientLine: View {
    let text: String

    var body: some View {
        HStack(spacing: 10) {
            if let artwork = ComponentArtwork.ingredient(for: text) {
                ComponentArtworkView(name: artwork.assetName).frame(width: 32, height: 32)
            } else {
                Image(systemName: "circle.fill").font(.system(size: 6)).foregroundStyle(ReceitasTheme.amber)
                    .frame(width: 32, height: 32)
            }
            Text(text).foregroundStyle(ReceitasTheme.crust)
        }
        .font(.body)
        .accessibilityElement(children: .combine)
    }
}

private struct NumberedStepLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(alignment: .top, spacing: 10) {
            configuration.icon.foregroundStyle(ReceitasTheme.ember)
            configuration.title.foregroundStyle(ReceitasTheme.crust)
        }
    }
}

struct CookingModeView: View {
    let recipe: Recipe
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
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
                .padding(horizontalSizeClass == .regular ? 30 : 16)
                .frame(maxWidth: 980, minHeight: horizontalSizeClass == .regular ? 600 : nil)
                .frame(maxWidth: .infinity)
        }
        .background(ReceitasTheme.cream.opacity(0.44))
        .navigationTitle("Modo cozinhar")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .tabBar)
        .onAppear { if !timer.isRunning { timer.start(at: Date()) } }
        .onReceive(tick) { date in refreshTimer(at: date) }
        .onChange(of: scenePhase) { _, phase in if phase == .active { refreshTimer(at: Date()) } }
        .alert("Tempo concluído", isPresented: $showTimerCompletion) { Button("OK", role: .cancel) {} } message: { Text("Este passo está pronto. Confira o alimento antes de continuar.") }
    }

    @ViewBuilder
    private var cookingView: some View {
        if horizontalSizeClass == .regular {
            HStack(alignment: .center, spacing: 42) {
                stepContent
                    .frame(maxWidth: 470, alignment: .leading)
                if timer.duration > 0 {
                    timerPanel.frame(maxWidth: 390)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 580, alignment: .center)
        } else {
            VStack(spacing: 24) {
                stepContent
                if timer.duration > 0 { timerPanel }
            }
            .frame(maxWidth: .infinity, minHeight: 470, alignment: .center)
        }
    }

    private var stepContent: some View {
        VStack(alignment: horizontalSizeClass == .regular ? .leading : .center, spacing: 22) {
            VStack(alignment: horizontalSizeClass == .regular ? .leading : .center, spacing: 10) {
                ProgressView(value: Double(step + 1), total: Double(max(1, recipe.steps.count)))
                    .tint(ReceitasTheme.ember)
                    .accessibilityLabel("Progresso da receita")
                    .accessibilityValue("Passo \(step + 1) de \(recipe.steps.count)")
                Text("Passo \(step + 1) de \(recipe.steps.count)")
                    .font(.headline)
                    .foregroundStyle(ReceitasTheme.ember)
                Text(recipe.steps[step].instruction)
                    .font(ReceitasTheme.display(horizontalSizeClass == .regular ? 28 : 24, weight: .semibold))
                    .multilineTextAlignment(horizontalSizeClass == .regular ? .leading : .center)
                    .foregroundStyle(ReceitasTheme.crust)
                    .fixedSize(horizontal: false, vertical: true)
            }
            toolHint
            stepNavigation
        }
    }

    private var toolHint: some View {
        let tool = ComponentArtwork.tool(for: recipe.steps[step].instruction)
        return HStack(spacing: 12) {
            ComponentArtworkView(name: tool.assetName).frame(width: 58, height: 58)
            VStack(alignment: .leading, spacing: 4) {
                Text("Dica da Vovó · \(tool.title)")
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(ReceitasTheme.crust)
                Text(tool.tip)
                    .font(.caption)
                    .foregroundStyle(ReceitasTheme.crust.opacity(0.72))
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(.white.opacity(0.88), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }

    private var timerPanel: some View {
        VStack(spacing: 20) {
            HStack(spacing: 8) {
                Image(systemName: "timer").font(.title2.weight(.semibold)).foregroundStyle(ReceitasTheme.ember)
                Text("Timer do passo").font(.headline).foregroundStyle(ReceitasTheme.crust)
            }
            Text(formattedTime)
                .font(.system(size: horizontalSizeClass == .regular ? 92 : 76, weight: .bold, design: .rounded))
                .monospacedDigit()
                .minimumScaleFactor(0.7)
                .lineLimit(1)
                .accessibilityLabel("Tempo restante")
                .accessibilityValue("\(timer.remainingSeconds / 60) minutos e \(timer.remainingSeconds % 60) segundos")
            HStack(spacing: 12) {
                if timer.remainingSeconds > 0 {
                    Button(timer.isRunning ? "Pausar" : "Continuar") { toggleTimer() }
                        .buttonStyle(.bordered)
                        .tint(ReceitasTheme.ember)
                }
                Button("Reiniciar") { timer.reset(startingAt: Date()) }
                    .buttonStyle(.bordered)
                    .tint(ReceitasTheme.ember)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(horizontalSizeClass == .regular ? 30 : 22)
        .padding(.vertical, horizontalSizeClass == .regular ? 34 : 24)
        .background(
            LinearGradient(colors: [ReceitasTheme.cream, .white], startPoint: .topLeading, endPoint: .bottomTrailing),
            in: RoundedRectangle(cornerRadius: 28, style: .continuous)
        )
        .overlay(RoundedRectangle(cornerRadius: 28, style: .continuous).stroke(ReceitasTheme.amber.opacity(0.18), lineWidth: 1))
        .accessibilityElement(children: .contain)
    }

    private var stepNavigation: some View {
        HStack(spacing: 14) {
            if step > 0 {
                Button("Anterior") { move(to: step - 1) }
                    .buttonStyle(.bordered)
                    .tint(ReceitasTheme.ember)
            }
            Button(step + 1 == recipe.steps.count ? "Concluir" : "Próximo") { advance() }
                .buttonStyle(.borderedProminent)
                .tint(ReceitasTheme.ember)
        }
        .frame(maxWidth: .infinity, alignment: horizontalSizeClass == .regular ? .leading : .center)
    }

    private var completionView: some View {
        VStack(spacing: 24) {
            ComponentArtworkView(name: ComponentArtwork.star).frame(width: 104, height: 104)
            Text("Receita concluída").font(.largeTitle.bold())
            Text("Bom apetite! Sirva com cuidado e confira a temperatura do alimento.").multilineTextAlignment(.center).foregroundStyle(.secondary)
            Button("Voltar à receita") { dismiss() }.buttonStyle(.borderedProminent).controlSize(.large)
        }
        .frame(maxWidth: 600, minHeight: horizontalSizeClass == .regular ? 580 : 420)
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .contain)
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
