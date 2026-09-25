import SwiftUI
import UIKit

struct IngredientArtwork: Identifiable, Hashable {
    let id: String
    let title: String
    let searchTerm: String
    let assetName: String
}

struct KitchenToolArtwork: Identifiable, Hashable {
    let id: String
    let title: String
    let tip: String
    let assetName: String
}

enum ComponentArtwork {
    static let featuredDish = "component_dish_pastel"
    static let brandPlaque = "component_ui_brand_plaque"
    static let woodenHeart = "component_ui_heart_wood"
    static let quickBadge = "component_ui_badge_rapidinho"
    static let star = "component_ui_star"
    static let heart = "component_ui_heart_red"
    static let favoriteMedal = "component_ui_medal_mais_amada"
    static let crispyBadge = "component_ui_badge_crocante"
    static let ornateFrame = "component_ui_ornate_frame"
    static let ribbon = "component_ui_ribbon"
    static let rosemary = "component_ui_rosemary"
    static let recipeDishes: [String: String] = [
        "pao-queijo": "component_dish_pao_queijo",
        "batata-crocante": "component_dish_batata_crocante",
        "coxinha-asa": "component_dish_coxinha_asa",
        "frango-crocante": "component_dish_frango_assado",
        "pasteis-banana": "component_dish_pastel",
        "quibe": "component_dish_bolinhos",
        "legumes-mediterraneos": "component_dish_legumes",
        "torrada-caprese": "component_dish_torrada_caprese",
        "tilapia-limao": "component_dish_tilapia",
        "brownie-cacau": "component_dish_bolo",
    ]

    static let categoryArtwork: [String: String] = [
        "Batatas": "component_dish_batata_crocante",
        "Frango": "component_dish_frango_assado",
        "Peixes": "component_dish_tilapia",
        "Vegetarianas": "component_dish_legumes",
        "Lanches": "component_dish_pao_queijo",
        "Café da manhã": "component_dish_pao_queijo",
        "Doces": "component_dish_bolo",
        "Carnes": "component_dish_bolinhos",
        "Acompanhamentos": "component_dish_batata_crocante",
        "Fit": "component_dish_legumes",
    ]

    static let ingredients: [IngredientArtwork] = [
        .init(id: "tomate", title: "Tomate", searchTerm: "tomate", assetName: "component_ingredient_tomate"),
        .init(id: "alho", title: "Alho", searchTerm: "alho", assetName: "component_ingredient_alho"),
        .init(id: "cebola", title: "Cebola", searchTerm: "cebola", assetName: "component_ingredient_cebola"),
        .init(id: "batata", title: "Batata", searchTerm: "batata", assetName: "component_ingredient_batata"),
        .init(id: "cenoura", title: "Cenoura", searchTerm: "cenoura", assetName: "component_ingredient_cenoura"),
        .init(id: "alecrim", title: "Alecrim", searchTerm: "alecrim", assetName: "component_ingredient_alecrim"),
        .init(id: "ovo", title: "Ovo", searchTerm: "ovo", assetName: "component_ingredient_ovo"),
        .init(id: "manjericao", title: "Manjericão", searchTerm: "manjericao", assetName: "component_ingredient_manjericao"),
        .init(id: "limao", title: "Limão", searchTerm: "limao", assetName: "component_ingredient_limao"),
        .init(id: "pimentao", title: "Pimentão", searchTerm: "pimentao", assetName: "component_ingredient_pimentao"),
    ]

    static let kitchenTools: [KitchenToolArtwork] = [
        .init(id: "colher", title: "Colher", tip: "Misture os temperos antes de começar.", assetName: "component_utensil_colher"),
        .init(id: "tabua", title: "Tábua", tip: "Corte os ingredientes em pedaços parecidos.", assetName: "component_utensil_tabua"),
        .init(id: "pano", title: "Pano", tip: "Deixe a bancada pronta para cozinhar.", assetName: "component_utensil_pano"),
        .init(id: "luva", title: "Luva térmica", tip: "Proteja as mãos ao retirar o cesto quente.", assetName: "component_utensil_luva"),
        .init(id: "cesto", title: "Cesto", tip: "Deixe espaço para o ar circular.", assetName: "component_utensil_cesto"),
        .init(id: "temperos", title: "Temperos", tip: "Ajuste o sabor antes de levar à Airfryer.", assetName: "component_utensil_temperos"),
        .init(id: "espatula", title: "Espátula", tip: "Vire os alimentos com cuidado.", assetName: "component_utensil_espatula"),
        .init(id: "prato", title: "Prato", tip: "Deixe o prato de servir por perto.", assetName: "component_utensil_prato"),
        .init(id: "pincel", title: "Pincel", tip: "Espalhe uma camada fina de azeite.", assetName: "component_utensil_pincel"),
        .init(id: "caderno", title: "Caderno", tip: "Anote suas variações favoritas.", assetName: "component_utensil_caderno"),
    ]

    static var allAssetNames: [String] {
        let dishes = [
            "component_dish_pao_queijo", "component_dish_batata_crocante", "component_dish_coxinha_asa",
            "component_dish_frango_assado", "component_dish_pastel", "component_dish_bolinhos",
            "component_dish_legumes", "component_dish_torrada_caprese", "component_dish_tilapia", "component_dish_bolo",
        ]
        let interface = [brandPlaque, woodenHeart, quickBadge, star, heart, favoriteMedal, crispyBadge, ornateFrame, ribbon, rosemary]
        return dishes + ingredients.map(\.assetName) + kitchenTools.map(\.assetName) + interface
    }

    static func ingredientMatches(_ ingredients: [String]) -> [IngredientArtwork] {
        let text = ingredients.map(\.normalized).joined(separator: " ")
        return self.ingredients.filter { text.contains($0.searchTerm.normalized) }
    }

    static func ingredient(for text: String) -> IngredientArtwork? {
        let normalized = text.normalized
        return ingredients.first { normalized.contains($0.searchTerm.normalized) }
    }

    static func recipeImageName(for recipe: Recipe) -> String {
        recipeDishes[recipe.id] ?? categoryArtwork[recipe.category] ?? recipe.imageName ?? ""
    }

    static func badgeNames(for recipe: Recipe) -> [String] {
        var badges: [String] = []
        if recipe.minutes <= 15 { badges.append(quickBadge) }
        if recipe.name.normalized.contains("crocante") { badges.append(crispyBadge) }
        return badges
    }

    static func tool(for instruction: String) -> KitchenToolArtwork {
        let text = instruction.normalized
        if text.contains("corte") || text.contains("fatie") { return kitchenTools[1] }
        if text.contains("pincele") || text.contains("espalhe") { return kitchenTools[8] }
        if text.contains("vire") || text.contains("agite") || text.contains("distribua") { return kitchenTools[6] }
        if text.contains("retire") || text.contains("preaqueca") { return kitchenTools[3] }
        if text.contains("tempere") || text.contains("sal") || text.contains("pimenta") { return kitchenTools[5] }
        if text.contains("sirva") || text.contains("prato") { return kitchenTools[7] }
        if text.contains("misture") || text.contains("combine") { return kitchenTools[0] }
        return kitchenTools[4]
    }
}

struct ComponentArtworkView: View {
    let name: String
    var contentMode: ContentMode = .fit

    var body: some View {
        Group {
            if let image = RecipeBundleImage.load(name: name) {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: contentMode)
            } else {
                Image(systemName: "fork.knife")
                    .resizable()
                    .scaledToFit()
                    .foregroundStyle(ReceitasTheme.ember)
            }
        }
        .accessibilityHidden(true)
    }
}
