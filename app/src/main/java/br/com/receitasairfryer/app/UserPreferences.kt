package br.com.receitasairfryer.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val favorites: Set<String> = emptySet(),
    val pantry: Set<String> = emptySet(),
    val healthyFirst: Boolean = false,
    val keepScreenOn: Boolean = true,
    val dietaryFilters: Set<DietaryTag> = emptySet(),
    val filterCategory: String? = null,
    val filterQuickOnly: Boolean = false,
    val filterHealthyOnly: Boolean = false,
    val activeRecipeId: String? = null,
    val activeStep: Int = 0,
    val timerEndTimestamp: Long = 0L
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val favorites = stringSetPreferencesKey("favorites")
        val pantry = stringSetPreferencesKey("pantry")
        val healthyFirst = booleanPreferencesKey("healthy_first")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val dietaryFilters = stringSetPreferencesKey("dietary_filters")
        val filterCategory = stringPreferencesKey("filter_category")
        val filterQuickOnly = booleanPreferencesKey("filter_quick_only")
        val filterHealthyOnly = booleanPreferencesKey("filter_healthy_only")
        val activeRecipe = stringPreferencesKey("active_recipe")
        val activeStep = stringPreferencesKey("active_step")
        val timerEnd = longPreferencesKey("timer_end")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { values ->
        UserSettings(
            favorites = values[Keys.favorites].orEmpty(),
            pantry = values[Keys.pantry].orEmpty(),
            healthyFirst = values[Keys.healthyFirst] ?: false,
            keepScreenOn = values[Keys.keepScreenOn] ?: true,
            dietaryFilters = values[Keys.dietaryFilters].orEmpty().mapNotNull { runCatching { DietaryTag.valueOf(it) }.getOrNull() }.toSet(),
            filterCategory = values[Keys.filterCategory],
            filterQuickOnly = values[Keys.filterQuickOnly] ?: false,
            filterHealthyOnly = values[Keys.filterHealthyOnly] ?: false,
            activeRecipeId = values[Keys.activeRecipe],
            activeStep = values[Keys.activeStep]?.toIntOrNull() ?: 0,
            timerEndTimestamp = values[Keys.timerEnd] ?: 0L
        )
    }

    suspend fun toggleFavorite(id: String) = context.dataStore.edit { values ->
        val updated = values[Keys.favorites].orEmpty().toMutableSet()
        if (!updated.add(id)) updated.remove(id)
        values[Keys.favorites] = updated
    }

    suspend fun togglePantry(ingredient: String) = context.dataStore.edit { values ->
        val updated = values[Keys.pantry].orEmpty().toMutableSet()
        if (!updated.add(ingredient)) updated.remove(ingredient)
        values[Keys.pantry] = updated
    }

    suspend fun setHealthyFirst(enabled: Boolean) = context.dataStore.edit { it[Keys.healthyFirst] = enabled }
    suspend fun setKeepScreenOn(enabled: Boolean) = context.dataStore.edit { it[Keys.keepScreenOn] = enabled }
    suspend fun setFilters(category: String?, quickOnly: Boolean, healthyOnly: Boolean, dietary: Set<DietaryTag>) = context.dataStore.edit {
        if (category == null) it.remove(Keys.filterCategory) else it[Keys.filterCategory] = category
        it[Keys.filterQuickOnly] = quickOnly
        it[Keys.filterHealthyOnly] = healthyOnly
        it[Keys.dietaryFilters] = dietary.map { tag -> tag.name }.toSet()
    }

    suspend fun saveTimer(recipeId: String, step: Int, endTimestamp: Long) = context.dataStore.edit {
        it[Keys.activeRecipe] = recipeId
        it[Keys.activeStep] = step.toString()
        it[Keys.timerEnd] = endTimestamp
    }

    suspend fun clearTimer() = context.dataStore.edit {
        it.remove(Keys.activeRecipe)
        it.remove(Keys.activeStep)
        it.remove(Keys.timerEnd)
    }
}
