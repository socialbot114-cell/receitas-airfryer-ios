import SwiftUI
import UIKit

struct RootView: View {
    var body: some View {
        TabView {
            HomeView().tabItem { Label("Início", systemImage: "house.fill") }
            DiscoverView().tabItem { Label("Descobrir", systemImage: "magnifyingglass") }
            FavoritesView().tabItem { Label("Favoritos", systemImage: "heart.fill") }
            ProfileView().tabItem { Label("Perfil", systemImage: "person.crop.circle") }
        }.tint(ReceitasTheme.accent)
    }
}

struct HomeView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View {
        NavigationStack {
            Group {
                switch store.catalogState {
                case .failed(let message): CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                case .empty: CatalogUnavailableView(title: "Nenhuma receita", message: "O catálogo está vazio.", systemImage: "book.closed")
                case .loaded: homeContent
                }
            }
            .navigationTitle("Receitas Airfryer")
        }
    }

    private var homeContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                ZStack(alignment: .bottomLeading) {
                    RecipeBundleImage(name: "start_receitas_vovo")
                        .frame(height: 240)
                    LinearGradient(colors: [.clear, .black.opacity(0.62)], startPoint: .center, endPoint: .bottom)
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Cozinhe algo gostoso hoje")
                            .font(ReceitasTheme.display(28, weight: .bold))
                            .foregroundStyle(.white)
                        Text("Receitas práticas para sua Airfryer, sem complicação.")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.9))
                    }
                    .padding(18)
                }
                .accessibilityElement(children: .contain)
                .clipShape(RoundedRectangle(cornerRadius: 24))

                Text("Escolha uma receita").font(.title2.bold())
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 155, maximum: 280), spacing: 16)], spacing: 16) {
                    ForEach(Array(store.repository.payload.recipes.prefix(6))) { recipe in RecipeCard(recipe: recipe) }
                }
            }
            .frame(maxWidth: 1_000, alignment: .leading)
            .padding()
            .frame(maxWidth: .infinity)
        }
    }
}

struct DiscoverView: View {
    @EnvironmentObject private var store: RecipeStore
    @State private var query = ""; @State private var quick = false; @State private var healthy = false
    var body: some View {
        NavigationStack {
            Group {
                if case .failed(let message) = store.catalogState {
                    CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                } else {
                    List {
                        Section { TextField("Buscar receita ou ingrediente", text: $query).textInputAutocapitalization(.never); Toggle("Até 15 minutos", isOn: $quick); Toggle("Opções mais leves", isOn: $healthy) }
                        let results = store.repository.search(query, quickOnly: quick, healthyOnly: healthy)
                        if results.isEmpty {
                            ContentUnavailableView("Nenhum resultado", systemImage: "magnifyingglass", description: Text("Tente outros termos ou remova um filtro."))
                                .listRowBackground(Color.clear)
                        } else {
                            ForEach(results) { recipe in NavigationLink(destination: RecipeDetailView(recipe: recipe)) { RecipeRow(recipe: recipe) } }
                        }
                    }
                }
            }.navigationTitle("Descobrir")
        }
    }
}

struct FavoritesView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View {
        NavigationStack {
            let favorites = store.repository.payload.recipes.filter { store.isFavorite($0) }
            Group {
                if case .failed(let message) = store.catalogState {
                    CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                } else if favorites.isEmpty {
                    ContentUnavailableView("Sem favoritos", systemImage: "heart", description: Text("Toque no coração de uma receita para encontrá-la aqui."))
                } else {
                    List(favorites) { recipe in NavigationLink(destination: RecipeDetailView(recipe: recipe)) { RecipeRow(recipe: recipe) } }
                }
            }.navigationTitle("Favoritos")
        }
    }
}

struct ProfileView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View { NavigationStack { Form { Section("Sua cozinha") { Label("Receitas disponíveis", systemImage: "book.fill"); Text("\(store.repository.payload.recipes.count) receitas offline") }; Section("Sobre") { Text("Receitas Airfryer"); Text("App Store ID: 6813681707").foregroundStyle(.secondary) } }.navigationTitle("Perfil") } }
}

struct RecipeCard: View { let recipe: Recipe; var body: some View { NavigationLink(destination: RecipeDetailView(recipe: recipe)) { VStack(alignment: .leading, spacing: 8) { RecipeImage(recipe: recipe).frame(height: 130); Text(recipe.name).font(.headline).foregroundStyle(.primary).lineLimit(2); HStack(spacing: 10) { Label("\(recipe.minutes) min", systemImage: "clock"); Label("\(recipe.temperature)°C", systemImage: "flame") }.font(.caption).foregroundStyle(.secondary); HStack { Text(recipe.difficulty).font(.caption.weight(.semibold)).foregroundStyle(ReceitasTheme.gold); Spacer(); Text("\(recipe.servings) porções").font(.caption).foregroundStyle(.secondary) } }.padding(12).background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 18, style: .continuous)) }.buttonStyle(.plain).accessibilityLabel("\(recipe.name), \(recipe.minutes) minutos, \(recipe.temperature) graus") } }
struct RecipeRow: View { let recipe: Recipe; var body: some View { HStack { RecipeImage(recipe: recipe).frame(width: 72, height: 72); VStack(alignment: .leading) { Text(recipe.name).font(.headline); Text("\(recipe.category) • \(recipe.minutes) min").font(.subheadline).foregroundStyle(.secondary) } }.accessibilityElement(children: .combine) } }

struct RecipeBundleImage: View {
    let name: String
    var body: some View {
        Group {
            if let image = Self.load(name: name) { Image(uiImage: image).resizable().scaledToFill() }
            else { LinearGradient(colors: [ReceitasTheme.ember.opacity(0.8), ReceitasTheme.amber.opacity(0.5)], startPoint: .topLeading, endPoint: .bottomTrailing).overlay(Image(systemName: "fork.knife").font(.largeTitle).foregroundStyle(.white)) }
        }
    }
    static func load(name: String, bundle: Bundle = .main) -> UIImage? {
        if let image = UIImage(named: name, in: bundle, compatibleWith: nil) { return image }
        for fileExtension in ["png", "jpg", "jpeg"] {
            let url = bundle.url(forResource: name, withExtension: fileExtension, subdirectory: "Images")
                ?? bundle.url(forResource: name, withExtension: fileExtension)
            if let url, let image = UIImage(contentsOfFile: url.path) { return image }
        }
        return nil
    }
}

struct RecipeImage: View {
    let recipe: Recipe
    var body: some View {
        RecipeBundleImage(name: recipe.imageName ?? "")
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .accessibilityLabel("Foto de \(recipe.name)")
    }
}

struct CatalogUnavailableView: View {
    let title: String
    let message: String
    let systemImage: String
    var body: some View { ContentUnavailableView(title, systemImage: systemImage, description: Text(message)) }
}
