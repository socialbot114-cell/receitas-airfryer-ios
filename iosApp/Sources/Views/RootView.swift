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
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var columns: [GridItem] {
        Array(repeating: GridItem(.flexible(), spacing: 16), count: horizontalSizeClass == .regular ? 3 : 2)
    }

    var body: some View {
        NavigationStack {
            Group {
                switch store.catalogState {
                case .failed(let message): CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                case .empty: CatalogUnavailableView(title: "Nenhuma receita", message: "O catálogo está vazio.", systemImage: "book.closed")
                case .loaded: homeContent
                }
            }
            .toolbar(.hidden, for: .navigationBar)
        }
    }

    private var homeContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                heroCard

                HStack(spacing: 10) {
                    ComponentArtworkView(name: ComponentArtwork.ribbon)
                        .frame(width: 60, height: 34)
                    Text("Escolha uma receita")
                        .font(ReceitasTheme.display(22, weight: .bold))
                        .foregroundStyle(ReceitasTheme.crust)
                }
                .accessibilityElement(children: .combine)

                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(Array(store.repository.payload.recipes.prefix(6))) { recipe in
                        RecipeCard(recipe: recipe, imageHeight: horizontalSizeClass == .regular ? 170 : 122)
                    }
                }

                KitchenToolsCarousel()
            }
            .frame(maxWidth: 1_080, alignment: .leading)
            .padding()
            .frame(maxWidth: .infinity)
        }
    }

    private var heroCard: some View {
        let artworkSize: CGFloat = horizontalSizeClass == .regular ? 230 : 154
        return HStack(spacing: 8) {
            VStack(alignment: .leading, spacing: 9) {
                ComponentArtworkView(name: ComponentArtwork.brandPlaque)
                    .frame(width: horizontalSizeClass == .regular ? 112 : 88, height: horizontalSizeClass == .regular ? 66 : 52)
                Text("Cozinhe algo gostoso hoje")
                    .font(ReceitasTheme.display(horizontalSizeClass == .regular ? 28 : 22, weight: .bold))
                    .foregroundStyle(ReceitasTheme.crust)
                    .fixedSize(horizontal: false, vertical: true)
                Text("Receitas práticas para sua Airfryer, sem complicação.")
                    .font(.subheadline)
                    .foregroundStyle(ReceitasTheme.crust.opacity(0.72))
                    .fixedSize(horizontal: false, vertical: true)
            }
            Spacer(minLength: 0)
            ZStack {
                ComponentArtworkView(name: ComponentArtwork.ornateFrame)
                    .frame(width: artworkSize, height: artworkSize)
                ComponentArtworkView(name: ComponentArtwork.featuredDish)
                    .frame(width: artworkSize * 0.76, height: artworkSize * 0.76)
                    .padding(14)
                ComponentArtworkView(name: ComponentArtwork.rosemary)
                    .frame(width: 42, height: 42)
                    .rotationEffect(.degrees(-18))
                    .offset(x: artworkSize * 0.33, y: -artworkSize * 0.34)
            }
            .frame(width: artworkSize, height: artworkSize)
            .accessibilityHidden(true)
        }
        .padding(horizontalSizeClass == .regular ? 24 : 16)
        .frame(maxWidth: .infinity, minHeight: horizontalSizeClass == .regular ? 276 : 200)
        .background(
            LinearGradient(
                colors: [ReceitasTheme.cream, Color.white.opacity(0.94)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: RoundedRectangle(cornerRadius: 26, style: .continuous)
        )
        .overlay(RoundedRectangle(cornerRadius: 26, style: .continuous).stroke(ReceitasTheme.amber.opacity(0.18), lineWidth: 1))
        .shadow(color: ReceitasTheme.crust.opacity(0.10), radius: 18, y: 8)
        .accessibilityElement(children: .combine)
    }
}

struct DiscoverView: View {
    @EnvironmentObject private var store: RecipeStore
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var query = ""
    @State private var selectedCategory: String?
    @State private var quickOnly = false
    @State private var healthyOnly = false

    private var categories: [String] {
        Array(Set(store.repository.payload.recipes.map(\.category))).sorted()
    }

    private var columns: [GridItem] {
        Array(repeating: GridItem(.flexible(), spacing: 14), count: horizontalSizeClass == .regular ? 3 : 2)
    }

    var body: some View {
        NavigationStack {
            Group {
                if case .failed(let message) = store.catalogState {
                    CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            searchField
                            filterSection(title: "Categorias") {
                                FilterChip(title: "Todas", isSelected: selectedCategory == nil) { selectedCategory = nil }
                                ForEach(categories, id: \.self) { category in
                                    FilterChip(title: category, isSelected: selectedCategory == category) {
                                        selectedCategory = selectedCategory == category ? nil : category
                                    }
                                }
                            }
                            filterSection(title: "Ingredientes em destaque") {
                                ForEach(ComponentArtwork.ingredients) { ingredient in
                                    Button {
                                        query = ingredient.searchTerm
                                        selectedCategory = nil
                                    } label: {
                                        VStack(spacing: 5) {
                                            ComponentArtworkView(name: ingredient.assetName)
                                                .frame(width: 44, height: 44)
                                            Text(ingredient.title)
                                                .font(.caption.weight(.medium))
                                                .foregroundStyle(ReceitasTheme.crust)
                                        }
                                        .frame(width: 76, height: 78)
                                        .background(.white.opacity(0.78), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityLabel("Buscar receitas com \(ingredient.title)")
                                }
                            }
                            HStack(spacing: 10) {
                                FilterChip(title: "Até 15 minutos", isSelected: quickOnly) { quickOnly.toggle() }
                                FilterChip(title: "Mais leves", isSelected: healthyOnly) { healthyOnly.toggle() }
                            }

                            let results = store.repository.search(query, category: selectedCategory, quickOnly: quickOnly, healthyOnly: healthyOnly)
                            if results.isEmpty {
                                ContentUnavailableView("Nenhum resultado", systemImage: "magnifyingglass", description: Text("Tente outro ingrediente ou remova um filtro."))
                                    .frame(maxWidth: .infinity, minHeight: 220)
                            } else {
                                LazyVGrid(columns: columns, spacing: 14) {
                                    ForEach(results) { recipe in RecipeCard(recipe: recipe, imageHeight: horizontalSizeClass == .regular ? 168 : 118) }
                                }
                            }
                        }
                        .frame(maxWidth: 1_080, alignment: .leading)
                        .padding()
                        .frame(maxWidth: .infinity)
                    }
                }
            }
            .navigationTitle("Descobrir")
        }
    }

    private var searchField: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass").foregroundStyle(ReceitasTheme.ember)
            TextField("Buscar receita ou ingrediente", text: $query)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            if !query.isEmpty {
                Button { query = "" } label: { Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary) }
                    .accessibilityLabel("Limpar busca")
            }
        }
        .padding(14)
        .background(.white, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(ReceitasTheme.crust.opacity(0.08), lineWidth: 1))
    }

    private func filterSection<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(.headline).foregroundStyle(ReceitasTheme.crust)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 9, content: content)
            }
        }
    }
}

struct FavoritesView: View {
    @EnvironmentObject private var store: RecipeStore
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    var body: some View {
        NavigationStack {
            let favorites = store.repository.payload.recipes.filter { store.isFavorite($0) }
            ScrollView {
                Group {
                    if case .failed(let message) = store.catalogState {
                        CatalogUnavailableView(title: "Catálogo indisponível", message: message, systemImage: "exclamationmark.triangle")
                    } else if favorites.isEmpty {
                        VStack(spacing: 14) {
                            ComponentArtworkView(name: ComponentArtwork.woodenHeart).frame(width: 130, height: 130)
                            Text("Sua coleção começa aqui").font(ReceitasTheme.display(24, weight: .bold))
                            Text("Toque no coração de uma receita para guardar suas favoritas.")
                                .multilineTextAlignment(.center)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity, minHeight: 360)
                        .padding()
                    } else {
                        VStack(alignment: .leading, spacing: 18) {
                            HStack(spacing: 12) {
                                ComponentArtworkView(name: ComponentArtwork.favoriteMedal).frame(width: 78, height: 78)
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Suas mais amadas").font(ReceitasTheme.display(24, weight: .bold))
                                    Text("\(favorites.count) receitas guardadas").font(.subheadline).foregroundStyle(.secondary)
                                }
                            }
                            LazyVGrid(columns: gridColumns, spacing: 14) {
                                ForEach(favorites) { recipe in RecipeCard(recipe: recipe, imageHeight: horizontalSizeClass == .regular ? 168 : 118) }
                            }
                        }
                        .frame(maxWidth: 1_080, alignment: .leading)
                        .padding()
                        .frame(maxWidth: .infinity)
                    }
                }
            }
            .navigationTitle("Favoritos")
        }
    }

    private var gridColumns: [GridItem] {
        Array(repeating: GridItem(.flexible(), spacing: 14), count: horizontalSizeClass == .regular ? 3 : 2)
    }
}

struct ProfileView: View {
    @EnvironmentObject private var store: RecipeStore
    private var versionDescription: String {
        let info = Bundle.main.infoDictionary ?? [:]
        let version = info["CFBundleShortVersionString"] as? String ?? "—"
        let build = info["CFBundleVersion"] as? String ?? "—"
        return "Versão \(version) (\(build))"
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Sua cozinha") {
                    Label("Receitas disponíveis", systemImage: "book.fill")
                    Text("\(store.repository.payload.recipes.count) receitas offline")
                        .foregroundStyle(.secondary)
                }
                Section("Sobre") {
                    Label("Receitas Airfryer", systemImage: "sparkles")
                    Text(versionDescription).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Perfil")
        }
    }
}

struct RecipeCard: View {
    let recipe: Recipe
    var imageHeight: CGFloat = 132

    var body: some View {
        NavigationLink(destination: RecipeDetailView(recipe: recipe)) {
            VStack(alignment: .leading, spacing: 9) {
                ZStack(alignment: .topLeading) {
                    RecipeImage(recipe: recipe, height: imageHeight)
                    HStack(spacing: 4) {
                        ForEach(ComponentArtwork.badgeNames(for: recipe), id: \.self) { badge in
                            ComponentArtworkView(name: badge).frame(width: 84, height: 34)
                        }
                    }
                    .padding(6)
                }
                Text(recipe.name)
                    .font(.headline)
                    .foregroundStyle(.primary)
                    .lineLimit(2)
                    .minimumScaleFactor(0.88)
                HStack(spacing: 10) {
                    Label("\(recipe.minutes) min", systemImage: "clock")
                    Label("\(recipe.temperature)°C", systemImage: "flame")
                }
                .font(.caption)
                .foregroundStyle(.secondary)
                HStack {
                    Text(recipe.difficulty).font(.caption.weight(.semibold)).foregroundStyle(ReceitasTheme.gold)
                    Spacer(minLength: 4)
                    Text("\(recipe.servings) porções").font(.caption).foregroundStyle(.secondary)
                }
            }
            .padding(12)
            .background(.white, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(ReceitasTheme.crust.opacity(0.08), lineWidth: 1))
            .shadow(color: ReceitasTheme.crust.opacity(0.08), radius: 12, y: 5)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(recipe.name), \(recipe.minutes) minutos, \(recipe.temperature) graus")
    }
}

struct RecipeRow: View {
    let recipe: Recipe

    var body: some View {
        HStack(spacing: 12) {
            RecipeImage(recipe: recipe, height: 72)
                .frame(width: 76)
            VStack(alignment: .leading, spacing: 4) {
                Text(recipe.name).font(.headline)
                Text("\(recipe.category) • \(recipe.minutes) min").font(.subheadline).foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}

private struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(isSelected ? .white : ReceitasTheme.crust)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(isSelected ? ReceitasTheme.ember : ReceitasTheme.cream, in: Capsule())
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct KitchenToolsCarousel: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Text("Utensílios da Vovó").font(ReceitasTheme.display(21, weight: .bold))
                ComponentArtworkView(name: ComponentArtwork.star).frame(width: 30, height: 30)
            }
            Text("Pequenos cuidados deixam cada preparo melhor.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .top, spacing: 12) {
                    ForEach(ComponentArtwork.kitchenTools) { tool in
                        VStack(spacing: 8) {
                            ComponentArtworkView(name: tool.assetName).frame(width: 72, height: 72)
                            Text(tool.title).font(.caption.weight(.semibold)).foregroundStyle(ReceitasTheme.crust).lineLimit(1)
                            Text(tool.tip).font(.caption2).foregroundStyle(.secondary).multilineTextAlignment(.center).lineLimit(3)
                        }
                        .frame(width: 112, height: 158, alignment: .top)
                        .padding(10)
                        .background(.white, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                    }
                }
                .padding(.vertical, 2)
            }
        }
        .accessibilityElement(children: .contain)
    }
}

struct RecipeBundleImage: View {
    let name: String
    var body: some View {
        Group {
            if let image = Self.load(name: name) { Image(uiImage: image).resizable().scaledToFit() }
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
    var height: CGFloat = 130

    var body: some View {
        GeometryReader { geometry in
            RecipeBundleImage(name: ComponentArtwork.recipeImageName(for: recipe))
                .frame(width: geometry.size.width, height: geometry.size.height)
                .clipped()
        }
        .frame(height: height)
        .background(ReceitasTheme.cream.opacity(0.72))
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .accessibilityLabel("Ilustração de \(recipe.name)")
    }
}

struct CatalogUnavailableView: View {
    let title: String
    let message: String
    let systemImage: String
    var body: some View { ContentUnavailableView(title, systemImage: systemImage, description: Text(message)) }
}
