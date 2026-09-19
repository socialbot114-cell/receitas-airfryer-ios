import SwiftUI
import UIKit

struct RootView: View {
    var body: some View {
        TabView {
            HomeView().tabItem { Label("Início", systemImage: "house.fill") }
            DiscoverView().tabItem { Label("Descobrir", systemImage: "magnifyingglass") }
            FavoritesView().tabItem { Label("Favoritos", systemImage: "heart.fill") }
            ProfileView().tabItem { Label("Perfil", systemImage: "person.crop.circle") }
        }.tint(.orange)
    }
}

struct HomeView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Image("start_receitas_vovo").resizable().scaledToFill().frame(height: 180).clipShape(RoundedRectangle(cornerRadius: 24))
                    Text("Cozinhe algo gostoso hoje").font(.largeTitle.bold())
                    Text("Receitas práticas para sua Airfryer, sem complicação.").foregroundStyle(.secondary)
                    Text("Escolha uma receita").font(.title2.bold())
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        ForEach(Array(store.repository.payload.recipes.prefix(6))) { recipe in RecipeCard(recipe: recipe) }
                    }
                }.padding()
            }.navigationTitle("Receitas Airfryer")
        }
    }
}

struct DiscoverView: View {
    @EnvironmentObject private var store: RecipeStore
    @State private var query = ""; @State private var quick = false; @State private var healthy = false
    var body: some View {
        NavigationStack { List {
            Section { TextField("Buscar receita ou ingrediente", text: $query).textInputAutocapitalization(.never); Toggle("Até 15 minutos", isOn: $quick); Toggle("Opções mais leves", isOn: $healthy) }
            ForEach(store.repository.search(query, quickOnly: quick, healthyOnly: healthy)) { recipe in NavigationLink(destination: RecipeDetailView(recipe: recipe)) { RecipeRow(recipe: recipe) } }
        }.navigationTitle("Descobrir") }
    }
}

struct FavoritesView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View { NavigationStack { List(store.repository.payload.recipes.filter { store.isFavorite($0) }) { recipe in NavigationLink(recipe.name, destination: RecipeDetailView(recipe: recipe)) } .navigationTitle("Favoritos") } }
}

struct ProfileView: View {
    @EnvironmentObject private var store: RecipeStore
    var body: some View { NavigationStack { Form { Section("Sua cozinha") { Label("Receitas disponíveis", systemImage: "book.fill"); Text("\(store.repository.payload.recipes.count) receitas offline") }; Section("Sobre") { Text("Receitas Airfryer"); Text("App Store ID: 6813681707").foregroundStyle(.secondary) } }.navigationTitle("Perfil") } }
}

struct RecipeCard: View { let recipe: Recipe; var body: some View { NavigationLink(destination: RecipeDetailView(recipe: recipe)) { VStack(alignment: .leading) { RecipeImage(recipe: recipe).frame(height: 110); Text(recipe.name).font(.headline); Text("\(recipe.minutes) min • \(recipe.temperature)°C").font(.caption).foregroundStyle(.secondary) } }.buttonStyle(.plain) } }
struct RecipeRow: View { let recipe: Recipe; var body: some View { HStack { RecipeImage(recipe: recipe).frame(width: 64, height: 64); VStack(alignment: .leading) { Text(recipe.name).font(.headline); Text("\(recipe.category) • \(recipe.minutes) min").font(.caption).foregroundStyle(.secondary) } } } }
struct RecipeImage: View { let recipe: Recipe; var body: some View { Group { if let imageName = recipe.imageName, UIImage(named: imageName) != nil { Image(imageName).resizable().scaledToFill() } else { LinearGradient(colors: [.orange.opacity(0.8), .yellow.opacity(0.5)], startPoint: .topLeading, endPoint: .bottomTrailing).overlay(Image(systemName: "fork.knife").font(.largeTitle).foregroundStyle(.white)) } }.clipShape(RoundedRectangle(cornerRadius: 14)) } }
