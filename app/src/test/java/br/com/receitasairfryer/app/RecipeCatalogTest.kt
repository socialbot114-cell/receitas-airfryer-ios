package br.com.receitasairfryer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeCatalogTest {
    @Test
    fun catalog_hasCompleteUniqueRecipes() {
        assertTrue(RecipeCatalog.recipes.size >= 20)
        assertEquals(RecipeCatalog.recipes.size, RecipeCatalog.recipes.map { it.id }.distinct().size)
        RecipeCatalog.recipes.forEach { recipe ->
            assertTrue("${recipe.name} sem ingredientes", recipe.ingredients.isNotEmpty())
            assertTrue("${recipe.name} sem passos suficientes", recipe.steps.size >= 4)
            assertTrue(recipe.minutes > 0)
            assertTrue(recipe.temperature in 160..205)
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
        assertTrue(sweets.size >= 8)
        assertTrue(sweets.all { it.category == "Doces" && it.ingredients.isNotEmpty() && it.steps.size >= 4 })
        assertTrue(RecipeCatalog.search("", dietary = setOf(DietaryTag.DOCES, DietaryTag.SEM_GLUTEN)).all { DietaryTag.DOCES in it.dietaryTags })
        assertTrue(RecipeCatalog.recipes.filter { it.allergens.isNotEmpty() }.all { it.allergens.all { allergen -> allergen in setOf("LEITE", "GLUTEN", "OVO", "OLEAGINOSAS") } })
    }

    @Test
    fun pantryRanking_prioritizesMostMatchesThenFastest() {
        val ranked = RecipeCatalog.rankedByPantry(setOf("frango", "alho", "limão", "azeite"))
        assertEquals(4, ranked.first().second)
        assertEquals("coxinha-asa", ranked.first().first.id)
        ranked.zipWithNext().forEach { (first, second) -> assertTrue(first.second >= second.second) }
    }

    @Test
    fun remainingTimer_usesTimestampAndNeverBecomesNegative() {
        assertEquals(8, remainingSeconds(18_500L, 10_000L))
        assertEquals(0, remainingSeconds(9_000L, 10_000L))
    }
}
