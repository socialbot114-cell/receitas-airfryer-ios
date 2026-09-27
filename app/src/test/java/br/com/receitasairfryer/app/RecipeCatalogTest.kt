package br.com.receitasairfryer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeCatalogTest {
    @Test
    fun catalog_hasCompleteUniqueRecipes() {
        assertEquals(300, RecipeCatalog.recipes.size)
        assertEquals(RecipeCatalog.recipes.size, RecipeCatalog.recipes.map { it.id }.distinct().size)
        assertEquals(RecipeCatalog.recipes.size, RecipeCatalog.recipes.map { it.name.normalized() }.distinct().size)
        RecipeCatalog.recipes.forEach { recipe ->
            assertTrue("${recipe.name} sem ingredientes", recipe.ingredients.isNotEmpty())
            assertTrue("${recipe.name} sem passos suficientes", recipe.steps.size >= 4)
            assertTrue(recipe.minutes > 0)
            assertTrue(recipe.temperature in 100..205)
            assertTrue(recipe.servings > 0)
            assertTrue(recipe.calories > 0)
        }
    }

    @Test
    fun search_matchesAccentsIngredientsAndDeterministicFilters() {
        assertTrue(RecipeCatalog.search("brocolis").any { it.id == "brocolis-alho" })
        assertTrue(RecipeCatalog.search("limão").all { recipe -> recipe.ingredients.any { "limao" in it.normalized() } })
        assertTrue(RecipeCatalog.search("", quickOnly = true).all { it.minutes <= 15 })
        assertTrue(RecipeCatalog.search("", category = "Peixes").all { it.category == "Peixes" })
        assertTrue(RecipeCatalog.search("", healthyOnly = true).all { it.healthy })
        assertTrue(RecipeCatalog.search("", quickOnly = true, healthyOnly = true).all { it.minutes <= 15 && it.healthy })
    }

    @Test
    fun sweets_areCompleteAndSupportCombinedDietaryFilters() {
        val sweets = RecipeCatalog.search("", dietary = setOf(DietaryTag.DOCES))
        assertEquals(58, sweets.size)
        assertTrue(sweets.all { it.category == "Doces" && it.ingredients.isNotEmpty() && it.steps.size >= 4 })
        assertTrue(RecipeCatalog.search("", dietary = setOf(DietaryTag.DOCES, DietaryTag.SEM_GLUTEN)).all { DietaryTag.DOCES in it.dietaryTags })
        assertTrue(RecipeCatalog.recipes.filter { it.allergens.isNotEmpty() }.all { it.allergens.all { allergen -> allergen in setOf("LEITE", "GLUTEN", "OVO", "OLEAGINOSAS", "CRUSTACEOS", "PEIXES", "MOLUSCOS", "SOJA", "GERGELIM") } })
    }

    @Test
    fun newRecipes_areSearchableAndIncludeRelevantAllergens() {
        assertTrue(RecipeCatalog.search("batata-doce").any { it.id == "batata-doce-palitos" })
        assertTrue("CRUSTACEOS" in RecipeCatalog.recipes.first { it.id == "camarao-alho-limao" }.allergens)
        assertTrue("GLUTEN" in RecipeCatalog.recipes.first { it.id == "pao-alho" }.allergens)
        assertFalse("GLUTEN" in RecipeCatalog.recipes.first { it.id == "arepa-queijo" }.allergens)
        assertFalse("GLUTEN" in RecipeCatalog.recipes.first { it.id == "bolinho-ervilha-hortela" }.allergens)
        assertTrue("PEIXES" in RecipeCatalog.recipes.first { it.id == "truta-amendoas" }.allergens)
        assertTrue("MOLUSCOS" in RecipeCatalog.recipes.first { it.id == "paella-ramequim-frutos-mar" }.allergens)
    }

    @Test
    fun pantryRanking_prioritizesMostMatchesThenFastest() {
        val ranked = RecipeCatalog.rankedByPantry(setOf("frango", "alho", "limão", "azeite"))
        assertEquals(4, ranked.first().second)
        assertEquals("coracao-frango-limao-oregano", ranked.first().first.id)
        ranked.zipWithNext().forEach { (first, second) -> assertTrue(first.second >= second.second) }
    }

    @Test
    fun remainingTimer_usesTimestampAndNeverBecomesNegative() {
        assertEquals(8, remainingSeconds(18_500L, 10_000L))
        assertEquals(0, remainingSeconds(9_000L, 10_000L))
    }
}
