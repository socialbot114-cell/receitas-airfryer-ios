package br.com.receitasairfryer.app

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Orange = Color(0xFFF26A21)
private val PaleOrange = Color(0xFFFFE5D2)
private val Gold = Color(0xFFFFC857)
private val Ink = Color(0xFF24211F)
private val Muted = Color(0xFF746E68)
private val Cream = Color(0xFFFAF8F5)
private val Green = Color(0xFF337A4C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = UserPreferences(applicationContext)
        setContent { AirfryerApp(preferences) }
    }
}

@Composable
private fun AirfryerApp(preferences: UserPreferences) {
    val settings by preferences.settings.collectAsState(initial = UserSettings())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var route by rememberSaveable { mutableStateOf("home") }
    var selectedId by rememberSaveable { mutableStateOf(RecipeCatalog.recipes.first().id) }
    var initialQuery by rememberSaveable { mutableStateOf("") }
    val selected = RecipeCatalog.recipes.first { it.id == selectedId }
    val scheme = MaterialTheme.colorScheme.copy(primary = Orange, secondary = Gold, background = Cream, surface = Color.White, onSurface = Ink)

    BackHandler(enabled = route != "home" && route != "discover" && route != "guide" && route != "favorites" && route != "profile") {
        route = when (route) { "cook" -> "detail"; "detail", "pantry" -> "home"; else -> "home" }
    }

    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = Cream) {
            Scaffold(
                containerColor = Cream,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
                bottomBar = { if (route in setOf("home", "discover", "guide", "favorites", "profile")) BottomNav(route) { route = it } }
            ) { padding ->
                when {
                    route == "detail" -> DetailScreen(selected, selected.id in settings.favorites, {
                        val wasFavorite = selected.id in settings.favorites
                        scope.launch {
                            preferences.toggleFavorite(selected.id)
                            val result = snackbarHostState.showSnackbar(
                                if (wasFavorite) "Receita removida dos favoritos" else "Receita salva nos favoritos",
                                actionLabel = "DESFAZER"
                            )
                            if (result == SnackbarResult.ActionPerformed) preferences.toggleFavorite(selected.id)
                        }
                    }, { route = "cook" }, { route = "home" }, Modifier.padding(padding))
                    route == "cook" -> CookScreen(selected, settings, preferences, { route = "detail" }, Modifier.padding(padding))
                    route == "pantry" -> PantryScreen(settings, preferences, onRecipe = { selectedId = it.id; route = "detail" }, onBack = { route = "home" }, Modifier.padding(padding))
                    else -> {
                    when (route) {
                        "discover" -> DiscoverScreen(initialQuery, settings, preferences, padding, onRecipe = { selectedId = it.id; route = "detail" })
                        "guide" -> GuideScreen(padding)
                        "favorites" -> FavoritesScreen(settings.favorites, padding) { selectedId = it.id; route = "detail" }
                        "profile" -> ProfileScreen(settings, preferences, padding)
                        else -> HomeScreen(settings, padding, onRecipe = { selectedId = it.id; route = "detail" }, onPantry = { route = "pantry" }, onDiscover = { query -> initialQuery = query; route = "discover" })
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun BottomNav(current: String, onNavigate: (String) -> Unit) {
    val tabs = listOf(
        Triple("home", stringResource(R.string.home), Icons.Outlined.Home), Triple("discover", stringResource(R.string.discover), Icons.AutoMirrored.Outlined.MenuBook),
        Triple("guide", stringResource(R.string.guide), Icons.Outlined.Timer), Triple("favorites", stringResource(R.string.favorites), Icons.Outlined.BookmarkBorder), Triple("profile", stringResource(R.string.profile), Icons.Outlined.PersonOutline)
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
        tabs.forEach { (route, label, icon) ->
            NavigationBarItem(selected = current == route, onClick = { onNavigate(route) }, icon = { Icon(icon, label) }, label = { Text(label, fontSize = 10.sp, maxLines = 1) })
        }
    }
}

@Composable
private fun HomeScreen(settings: UserSettings, padding: PaddingValues, onRecipe: (Recipe) -> Unit, onPantry: () -> Unit, onDiscover: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val featured = RecipeCatalog.recipes.sortedWith(compareByDescending<Recipe> { it.healthy == settings.healthyFirst }.thenBy { it.minutes })
    LazyColumn(contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 22.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            Text("COZINHA DESCOMPLICADA", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
            Text("O que vamos preparar?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Ink)
            Text("Receitas testáveis, tempos claros e tudo disponível offline.", color = Muted)
            Spacer(Modifier.height(16.dp))
            SearchField(query, { query = it }, stringResource(R.string.search_recipes), onSearch = { onDiscover(query) })
        }
        item {
            Card(Modifier.fillMaxWidth().clickable(onClick = onPantry), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Ink)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).background(Orange, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = Color.White) }
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text("Tenho em casa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(if (settings.pantry.isEmpty()) "Selecione ingredientes e veja os melhores matches" else "${settings.pantry.size} ingredientes selecionados", color = Color(0xFFD8D2CD), fontSize = 13.sp) }
                    Icon(Icons.Outlined.ChevronRight, "Abrir seleção", tint = Gold)
                }
            }
        }
        item { SectionHeader("Escolhas rápidas", "Ver catálogo") { onDiscover("") } }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(featured.take(6), key = { it.id }) { CompactRecipeCard(it, onRecipe) } } }
        item { SectionHeader("Receita em destaque", null) {} }
        item { HeroRecipeCard(featured.first { it.id == "banana-canela" }, onRecipe) }
        item { SectionHeader("Até 15 minutos", "Ver todas") { onDiscover("") } }
        items(RecipeCatalog.recipes.filter { it.minutes <= 15 }.take(4), key = { it.id }) { RecipeRow(it, onRecipe, it.id in settings.favorites) }
    }
}

@Composable
private fun DiscoverScreen(initialQuery: String, settings: UserSettings, preferences: UserPreferences, padding: PaddingValues, onRecipe: (Recipe) -> Unit) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable(initialQuery) { mutableStateOf(initialQuery) }
    var category by rememberSaveable { mutableStateOf(settings.filterCategory) }
    var quickOnly by rememberSaveable { mutableStateOf(settings.filterQuickOnly) }
    var healthyOnly by rememberSaveable { mutableStateOf(settings.filterHealthyOnly) }
    var dietary by remember { mutableStateOf(settings.dietaryFilters) }
    var draftCategory by rememberSaveable { mutableStateOf(settings.filterCategory) }
    var draftQuickOnly by rememberSaveable { mutableStateOf(settings.filterQuickOnly) }
    var draftHealthyOnly by rememberSaveable { mutableStateOf(settings.filterHealthyOnly) }
    var draftDietary by remember { mutableStateOf(settings.dietaryFilters) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val results = RecipeCatalog.search(query, category, quickOnly, healthyOnly, dietary)
    val categories = RecipeCatalog.recipes.map { it.category }.distinct().sorted()
    LazyColumn(contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Descobrir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Text("${results.size} receitas encontradas", color = Muted); Spacer(Modifier.height(14.dp)); SearchField(query, { query = it }, "Receita, ingrediente ou categoria") }
        item { OutlinedButton(onClick = { draftCategory = category; draftQuickOnly = quickOnly; draftHealthyOnly = healthyOnly; draftDietary = dietary; showFilters = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Outlined.Tune, "Filtros"); Spacer(Modifier.width(8.dp)); Text("FILTROS${(listOfNotNull(category) + dietary + listOf(quickOnly, healthyOnly).filter { it }).let { if (it.isEmpty()) "" else " (${it.size})" }}") } }
        if (results.isEmpty()) item { EmptyState("Nenhuma receita encontrada", "Tente remover um filtro ou buscar outro ingrediente.") }
        items(results, key = { it.id }) { RecipeRow(it, onRecipe, false) }
    }
    if (showFilters) FilterSheet(categories, draftCategory, draftQuickOnly, draftHealthyOnly, draftDietary, { draftCategory = it }, { draftQuickOnly = it }, { draftHealthyOnly = it }, { draftDietary = if (it in draftDietary) draftDietary - it else draftDietary + it }, { draftCategory = null; draftQuickOnly = false; draftHealthyOnly = false; draftDietary = emptySet() }, { category = draftCategory; quickOnly = draftQuickOnly; healthyOnly = draftHealthyOnly; dietary = draftDietary; scope.launch { preferences.setFilters(category, quickOnly, healthyOnly, dietary) }; showFilters = false }, { showFilters = false })
}

@Composable
private fun PantryScreen(settings: UserSettings, preferences: UserPreferences, onRecipe: (Recipe) -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val options = listOf("frango", "carne", "peixe", "batata", "ovo", "queijo", "tomate", "cebola", "alho", "azeite", "pao", "abobrinha", "brocolis", "banana", "aveia", "mandioca", "berinjela", "grao-de-bico")
    var showIngredients by rememberSaveable { mutableStateOf(false) }
    val ranked = RecipeCatalog.rankedByPantry(settings.pantry)
    LazyColumn(contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = modifier.fillMaxSize()) {
        item { ScreenHeader("O que tem na sua cozinha?", "Marque os ingredientes disponíveis. O ranking mostra coincidências reais.", onBack) }
        item { Button(onClick = { showIngredients = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Orange)) { Icon(Icons.Outlined.Inventory2, "Ingredientes"); Spacer(Modifier.width(8.dp)); Text(if (settings.pantry.isEmpty()) "SELECIONAR INGREDIENTES" else "${settings.pantry.size} INGREDIENTES SELECIONADOS") } }
        item { Text(if (settings.pantry.isEmpty()) "Selecione ao menos um ingrediente" else "Melhores combinações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (settings.pantry.isEmpty()) item { EmptyState("Sua seleção está vazia", "Toque nos ingredientes acima para criar um ranking personalizado.") }
        else items(ranked, key = { it.first.id }) { (recipe, matches) -> MatchRecipeCard(recipe, matches, onRecipe) }
    }
    if (showIngredients) IngredientSheet(options, settings.pantry, { scope.launch { preferences.togglePantry(it) } }, { showIngredients = false })
}

@Composable
private fun FavoritesScreen(favorites: Set<String>, padding: PaddingValues, onRecipe: (Recipe) -> Unit) {
    val saved = RecipeCatalog.recipes.filter { it.id in favorites }
    LazyColumn(contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Favoritos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Text("Suas receitas salvas ficam disponíveis offline.", color = Muted) }
        if (saved.isEmpty()) item { EmptyState("Nenhuma receita salva", "Abra uma receita e toque no marcador para encontrá-la aqui.") }
        items(saved, key = { it.id }) { RecipeRow(it, onRecipe, true) }
    }
}

@Composable
private fun GuideScreen(padding: PaddingValues) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = RecipeCatalog.guide.filter { query.normalized().isBlank() || query.normalized() in (it.name + it.cut).normalized() }
    LazyColumn(contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Guia Air Fryer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Text("Referência rápida para porções domésticas. Ajuste ao seu aparelho.", color = Muted); Spacer(Modifier.height(14.dp)); SearchField(query, { query = it }, "Pesquisar alimento") }
        items(results, key = { it.name }) { item -> Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.LocalFireDepartment, null, tint = Orange); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(item.cut, color = Muted, fontSize = 12.sp) }; Text("${item.temperature}°C", fontWeight = FontWeight.Black, color = Ink); Spacer(Modifier.width(12.dp)); Text(item.time, color = Orange, fontWeight = FontWeight.Bold) }; HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFEEE9E5)); Text(item.tip, color = Muted, fontSize = 13.sp) } } }
    }
}

@Composable
private fun ProfileScreen(settings: UserSettings, preferences: UserPreferences, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 18.dp, 20.dp, padding.calculateBottomPadding() + 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Perfil e ajustes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Text("Preferências locais, sem conta e sem coleta de dados.", color = Muted) }
        item { SettingsCard("Priorizar receitas leves", "Exibe opções marcadas como leves primeiro na Home.", settings.healthyFirst) { scope.launch { preferences.setHealthyFirst(it) } } }
        item { SettingsCard("Manter tela ativa ao cozinhar", "Evita que a tela apague enquanto o modo cozinhar estiver aberto.", settings.keepScreenOn) { scope.launch { preferences.setKeepScreenOn(it) } } }
        item { Card(colors = CardDefaults.cardColors(Ink), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Receitas Airfryer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("Versão 1.4.0 (6)", color = Gold); Text("30 receitas completas • 100% offline • sem anúncios", color = Color(0xFFD8D2CD)); Text("Tempos variam conforme potência, quantidade e modelo. Para carnes, use termômetro culinário.", color = Color(0xFFD8D2CD), fontSize = 12.sp) } } }
    }
}

@Composable
private fun DetailScreen(recipe: Recipe, favorite: Boolean, onFavorite: () -> Unit, onStart: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(contentPadding = PaddingValues(bottom = 34.dp), modifier = modifier.fillMaxSize()) {
        item { Row(Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }; Text("Receita", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.BookmarkBorder, if (favorite) "Remover dos favoritos" else "Adicionar aos favoritos", tint = Orange) } } }
        item { RecipeArtwork(recipe, Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 18.dp).clip(RoundedCornerShape(28.dp))) }
        if (recipe.dietaryTags.isNotEmpty()) item { Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { recipe.dietaryTags.take(5).forEach { InfoPill(tagLabel(it)) } } }
        if (recipe.allergens.isNotEmpty()) item { Text("Alérgenos: ${recipe.allergens.joinToString()}. Confira rótulos e contaminação cruzada.", Modifier.padding(horizontal = 20.dp), color = Muted, fontSize = 12.sp) }
        item { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(recipe.category.uppercase(), color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text(recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Text(recipe.description, color = Muted, lineHeight = 22.sp); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Stat("TEMPO", "${recipe.minutes} min"); Stat("TEMP.", "${recipe.temperature}°C"); Stat("PORÇÕES", recipe.servings.toString()); Stat("ESTIMATIVA", "${recipe.calories} kcal") }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { InfoPill(if (recipe.preheat) "Pré-aqueça" else "Sem pré-aquecer"); InfoPill(if (recipe.turn) "Vire na metade" else "Não precisa virar") }; Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(Orange), shape = RoundedCornerShape(18.dp)) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("COMEÇAR A COZINHAR", fontWeight = FontWeight.Bold) }; ContentTitle("Ingredientes"); recipe.ingredients.forEach { BulletLine(it) }; ContentTitle("Modo de preparo"); recipe.steps.forEachIndexed { index, step -> Row(verticalAlignment = Alignment.Top) { Box(Modifier.size(28.dp).background(PaleOrange, CircleShape), contentAlignment = Alignment.Center) { Text("${index + 1}", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(step.instruction, lineHeight = 21.sp); if (step.minutes > 0) Text("${step.minutes} min", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp) } } } } }
    }
}

@Composable
private fun CookScreen(recipe: Recipe, settings: UserSettings, preferences: UserPreferences, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(settings.keepScreenOn) {
        val previous = view.keepScreenOn
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = previous }
    }
    var stepIndex by rememberSaveable(recipe.id) { mutableIntStateOf(if (settings.activeRecipeId == recipe.id) settings.activeStep.coerceIn(recipe.steps.indices) else 0) }
    var confirmation by rememberSaveable { mutableStateOf<String?>(null) }
    var showTimerDone by rememberSaveable { mutableStateOf(false) }
    var showRecipeDone by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val step = recipe.steps[stepIndex]
    val timerEnd = if (settings.activeRecipeId == recipe.id && settings.activeStep == stepIndex) settings.timerEndTimestamp else 0L
    val remaining = remainingSeconds(timerEnd, now)
    LaunchedEffect(timerEnd) { while (timerEnd > System.currentTimeMillis()) { now = System.currentTimeMillis(); delay(500) }; now = System.currentTimeMillis() }
    LaunchedEffect(timerEnd, remaining == 0) { if (timerEnd > 0L && remaining == 0) showTimerDone = true }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }; Column { Text("Modo cozinhar", fontWeight = FontWeight.Bold); Text(recipe.name, color = Muted, fontSize = 12.sp) } }
        Text("PASSO ${stepIndex + 1} DE ${recipe.steps.size}", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color(0xFFE7E1DD))) { Box(Modifier.fillMaxWidth((stepIndex + 1f) / recipe.steps.size).height(5.dp).background(Orange)) }
        Text(step.instruction, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(Ink)) { Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("${recipe.temperature}°C", color = Gold, fontSize = 34.sp, fontWeight = FontWeight.Black); if (step.minutes > 0) { Text(formatTimer(if (timerEnd > 0) remaining else step.minutes * 60), color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Black); Text(if (timerEnd > 0 && remaining > 0) "RESTANTES" else if (timerEnd > 0) "TEMPO ENCERRADO" else "TEMPO DESTA ETAPA", color = Color(0xFFD8D2CD), fontSize = 11.sp, letterSpacing = 1.sp) } else { Icon(Icons.Outlined.Restaurant, null, tint = Gold, modifier = Modifier.size(56.dp)); Text("Prepare esta etapa e avance", color = Color.White) } } }
        if (step.minutes > 0 && timerEnd == 0L) Button(onClick = {
            if (Build.VERSION.SDK_INT >= 33) {
                (context as? Activity)?.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
            scope.launch { preferences.saveTimer(recipe.id, stepIndex, System.currentTimeMillis() + step.minutes * 60_000L) }
        }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(Orange)) { Icon(Icons.Outlined.Timer, null); Spacer(Modifier.width(8.dp)); Text("INICIAR TIMER", fontWeight = FontWeight.Bold) }
        if (timerEnd > 0L) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { confirmation = "restart" }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("REINICIAR") }
            OutlinedButton(onClick = { confirmation = "cancel" }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("CANCELAR") }
            Button(onClick = { scope.launch { preferences.saveTimer(recipe.id, stepIndex, maxOf(timerEnd, System.currentTimeMillis()) + 60_000L) } }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Green)) { Text("+ 1 MIN") }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { if (timerEnd > 0L && remaining > 0) confirmation = "advance" else if (stepIndex == recipe.steps.lastIndex) { scope.launch { preferences.clearTimer() }; showRecipeDone = true } else { stepIndex++; scope.launch { preferences.clearTimer() } } }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(if (stepIndex == recipe.steps.lastIndex) Green else Orange)) { Text(if (stepIndex == recipe.steps.lastIndex) "FINALIZAR RECEITA" else "CONCLUIR E AVANÇAR", fontWeight = FontWeight.Bold) }
        if (stepIndex > 0) OutlinedButton(onClick = { if (timerEnd > 0L && remaining > 0) confirmation = "previous" else { stepIndex--; scope.launch { preferences.clearTimer() } } }, Modifier.fillMaxWidth()) { Text("Etapa anterior") }
    }
    confirmation?.let { action ->
        val title = when (action) { "advance" -> "Avançar antes do tempo?"; "previous" -> "Voltar antes do tempo?"; "restart" -> "Reiniciar timer?"; else -> "Cancelar timer?" }
        AlertDialog(onDismissRequest = { confirmation = null }, title = { Text(title) }, text = { Text("O timer desta etapa ainda está ativo. O progresso do tempo será perdido." ) }, confirmButton = { TextButton(onClick = {
            confirmation = null
            when (action) {
                "restart" -> scope.launch { preferences.saveTimer(recipe.id, stepIndex, System.currentTimeMillis() + step.minutes * 60_000L) }
                "cancel" -> scope.launch { preferences.clearTimer() }
                "advance" -> { if (stepIndex == recipe.steps.lastIndex) showRecipeDone = true else stepIndex++; scope.launch { preferences.clearTimer() } }
                "previous" -> { stepIndex--; scope.launch { preferences.clearTimer() } }
            }
        }) { Text(if (action == "cancel") "CANCELAR TIMER" else "CONTINUAR") } }, dismissButton = { TextButton(onClick = { confirmation = null }) { Text("VOLTAR") } })
    }
    if (showTimerDone) PremiumSheet("Timer concluído", "A etapa terminou. Confira o ponto e continue quando estiver pronto.", "CONTINUAR", { showTimerDone = false }, { showTimerDone = false })
    if (showRecipeDone) PremiumSheet("Receita concluída", "Tudo pronto. Sirva enquanto está quente e aproveite o resultado.", "FECHAR", { showRecipeDone = false; onBack() }, { showRecipeDone = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, label: String, onSearch: (() -> Unit)? = null) {
    TextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(label, color = Muted) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, trailingIcon = if (onSearch != null) {{ IconButton(onClick = onSearch) { Icon(Icons.Outlined.ChevronRight, "Pesquisar") } }} else null, shape = RoundedCornerShape(18.dp), colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Orange))
}

@Composable
private fun RecipeArtwork(recipe: Recipe, modifier: Modifier = Modifier) {
    val imageResource = when (recipe.imageKey) {
        "asset_batata_crocante" -> R.drawable.asset_batata_crocante
        "asset_frango_crocante" -> R.drawable.asset_frango_crocante
        "asset_salmao_ervas" -> R.drawable.asset_salmao_ervas
        "asset_hamburguer_caseiro" -> R.drawable.asset_hamburguer_caseiro
        "asset_brocolis_alho" -> R.drawable.asset_brocolis_alho
        "asset_banana_canela" -> R.drawable.asset_banana_canela
        "asset_legumes_mediterraneos" -> R.drawable.asset_legumes_mediterraneos
        "asset_pao_queijo" -> R.drawable.asset_pao_queijo
        "asset_coxinha_asa" -> R.drawable.asset_coxinha_asa
        "asset_mandioca" -> R.drawable.asset_mandioca
        "asset_bife_acebolado" -> R.drawable.asset_bife_acebolado
        "asset_omelete" -> R.drawable.asset_omelete
        "asset_couve_flor" -> R.drawable.asset_couve_flor
        "asset_linguica_cebola" -> R.drawable.asset_linguica_cebola
        "asset_quibe" -> R.drawable.asset_quibe
        "asset_maca_crumble" -> R.drawable.asset_maca_crumble
        else -> null
    }
    if (imageResource != null) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = "Fotografia de ${recipe.name}",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        return
    }
    val palettes = listOf(Color(0xFFFFD3B6), Color(0xFFDDE7C7), Color(0xFFF4D6A2), Color(0xFFD4E4E7), Color(0xFFE8D4C8))
    val base = palettes[kotlin.math.abs(recipe.palette % palettes.size)]
    Canvas(modifier.background(base).semantics { contentDescription = "Ilustração original de ${recipe.name}" }) {
        drawCircle(Color.White.copy(alpha = .72f), radius = size.minDimension * .34f, center = center)
        drawCircle(Ink.copy(alpha = .10f), radius = size.minDimension * .30f, center = center, style = Stroke(size.minDimension * .025f))
        val food = when (recipe.category) { "Vegetarianas", "Fit" -> Green; "Doces", "Café da manhã" -> Gold; "Peixes" -> Color(0xFFF29B76); else -> Orange }
        repeat(7) { index ->
            val x = size.width * (.32f + (index % 3) * .18f)
            val y = size.height * (.35f + (index / 3) * .16f)
            drawOval(food.copy(alpha = .78f + (index % 2) * .12f), topLeft = Offset(x - size.width * .07f, y - size.height * .045f), size = Size(size.width * .15f, size.height * .10f))
        }
        drawCircle(Color(0xFFFAF3D3), size.minDimension * .055f, Offset(size.width * .52f, size.height * .48f))
        drawLine(Color.White.copy(alpha = .8f), Offset(size.width * .18f, size.height * .2f), Offset(size.width * .35f, size.height * .1f), strokeWidth = 5f)
    }
}

@Composable private fun HeroRecipeCard(recipe: Recipe, onClick: (Recipe) -> Unit) { Card(Modifier.fillMaxWidth().clickable { onClick(recipe) }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(Color.White)) { RecipeArtwork(recipe, Modifier.fillMaxWidth().height(180.dp)); Column(Modifier.padding(18.dp)) { Text(recipe.name, fontSize = 22.sp, fontWeight = FontWeight.Black); Text(recipe.description, color = Muted, maxLines = 2); Spacer(Modifier.height(10.dp)); RecipeMetadata(recipe) } } }
@Composable private fun CompactRecipeCard(recipe: Recipe, onClick: (Recipe) -> Unit) { Card(Modifier.width(170.dp).clickable { onClick(recipe) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White)) { RecipeArtwork(recipe, Modifier.fillMaxWidth().height(105.dp)); Column(Modifier.padding(12.dp)) { Text(recipe.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${recipe.minutes} min • ${recipe.temperature}°C", color = Orange, fontSize = 12.sp) } } }
@Composable private fun RecipeRow(recipe: Recipe, onClick: (Recipe) -> Unit, favorite: Boolean) { Card(Modifier.fillMaxWidth().clickable { onClick(recipe) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White)) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { RecipeArtwork(recipe, Modifier.size(94.dp).clip(RoundedCornerShape(15.dp))); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f)); if (favorite) Icon(Icons.Filled.Favorite, "Favorita", tint = Orange, modifier = Modifier.size(18.dp)) }; Text(recipe.mainIngredient + " • " + recipe.category, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(6.dp)); RecipeMetadata(recipe) }; Icon(Icons.Outlined.ChevronRight, null, tint = Orange) } } }
@Composable private fun MatchRecipeCard(recipe: Recipe, matches: Int, onClick: (Recipe) -> Unit) { Card(Modifier.fillMaxWidth().clickable { onClick(recipe) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { RecipeArtwork(recipe, Modifier.size(80.dp).clip(RoundedCornerShape(14.dp))); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(recipe.name, fontWeight = FontWeight.Bold); Text("Você tem $matches de ${recipe.pantryKeys.size} ingredientes-chave", color = if (matches == recipe.pantryKeys.size) Green else Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("${recipe.minutes} min • ${recipe.temperature}°C", color = Muted, fontSize = 12.sp) }; Icon(Icons.Outlined.ChevronRight, null, tint = Orange) } } }
@Composable private fun RecipeMetadata(recipe: Recipe) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.AccessTime, null, tint = Muted, modifier = Modifier.size(15.dp)); Text("${recipe.minutes} min", color = Muted, fontSize = 12.sp); Icon(Icons.Outlined.LocalFireDepartment, null, tint = Orange, modifier = Modifier.size(15.dp)); Text("${recipe.temperature}°C", color = Muted, fontSize = 12.sp) } }
@Composable private fun SectionHeader(title: String, action: String?, onClick: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); if (action != null) Text(action, color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) } }
@Composable private fun ScreenHeader(title: String, subtitle: String, onBack: () -> Unit) { Column { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }; Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }; Text(subtitle, color = Muted, modifier = Modifier.padding(start = 48.dp)) } }
@Composable private fun Stat(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Black, color = Ink); Text(label, color = Muted, fontSize = 9.sp) } }
@Composable private fun InfoPill(label: String) { Surface(color = PaleOrange, shape = CircleShape) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
private fun tagLabel(tag: DietaryTag): String = when (tag) {
    DietaryTag.SEM_LACTOSE, DietaryTag.LACTOSE_FREE -> "Sem lactose"
    DietaryTag.SEM_GLUTEN, DietaryTag.GLUTEN_FREE -> "Sem glúten"
    DietaryTag.VEGETARIANA, DietaryTag.VEGETARIAN -> "Vegetariana"
    DietaryTag.VEGANA -> "Vegana"
    DietaryTag.DOCES, DietaryTag.SWEET -> "Doces"
    DietaryTag.FIT -> "Fit"
}
@Composable private fun ContentTitle(value: String) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 6.dp)) }
@Composable private fun BulletLine(value: String) { Row(verticalAlignment = Alignment.Top) { Box(Modifier.padding(top = 8.dp).size(7.dp).background(Orange, CircleShape)); Spacer(Modifier.width(12.dp)); Text(value, lineHeight = 21.sp) } }
@Composable private fun EmptyState(title: String, body: String) { Column(Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(64.dp).background(PaleOrange, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Search, null, tint = Orange) }; Spacer(Modifier.height(14.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(body, color = Muted, modifier = Modifier.padding(top = 5.dp)) } }
@Composable private fun SettingsCard(title: String, body: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(body, color = Muted, fontSize = 12.sp) }; Switch(checked, onChecked) } } }
private fun formatTimer(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    categories: List<String>, category: String?, quickOnly: Boolean, healthyOnly: Boolean, dietary: Set<DietaryTag>,
    onCategory: (String?) -> Unit, onQuick: (Boolean) -> Unit, onHealthy: (Boolean) -> Unit, onDietary: (DietaryTag) -> Unit,
    onClear: () -> Unit, onApply: () -> Unit, onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, contentWindowInsets = { WindowInsets.safeDrawing }, containerColor = Cream, scrimColor = Ink.copy(alpha = .48f), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        Column(Modifier.padding(horizontal = 22.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Filtrar receitas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Combine opções para encontrar o preparo ideal.", color = Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(quickOnly, { onQuick(!quickOnly) }, label = { Text("Até 15 min") })
                FilterChip(healthyOnly, { onHealthy(!healthyOnly) }, label = { Text("Leves") })
            }
            Text("Categoria", fontWeight = FontWeight.Bold)
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { item -> FilterChip(selected = category == item, onClick = { onCategory(if (category == item) null else item) }, label = { Text(item) }, modifier = Modifier.fillMaxWidth()) }
            }
            Text("Preferências", fontWeight = FontWeight.Bold)
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DietaryTag.values().filter { it.ordinal < 6 }) { tag -> FilterChip(selected = tag in dietary, onClick = { onDietary(tag) }, label = { Text(tagLabel(tag)) }, modifier = Modifier.fillMaxWidth()) }
            }
            Text("Confira sempre os rótulos: receitas podem conter traços e sofrer contaminação cruzada.", color = Muted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("LIMPAR") }
                Button(onClick = onApply, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Orange)) { Text("APLICAR") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientSheet(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, contentWindowInsets = { WindowInsets.safeDrawing }, containerColor = Cream, scrimColor = Ink.copy(alpha = .48f), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        Column(Modifier.padding(horizontal = 22.dp).navigationBarsPadding()) {
            Text("Ingredientes disponíveis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Selecione o que você já tem em casa.", color = Muted, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(options) { ingredient ->
                    FilterChip(selected = ingredient in selected, onClick = { onToggle(ingredient) }, label = { Text(ingredient.replaceFirstChar(Char::uppercase), maxLines = 1, overflow = TextOverflow.Ellipsis) }, leadingIcon = if (ingredient in selected) {{ Icon(Icons.Outlined.CheckCircle, "Selecionado", Modifier.size(16.dp)) }} else null)
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Orange)) { Text("CONCLUIR SELEÇÃO") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumSheet(title: String, body: String, action: String, onAction: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, contentWindowInsets = { WindowInsets.safeDrawing }, containerColor = Cream, scrimColor = Ink.copy(alpha = .52f), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 18.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(42.dp).height(5.dp).background(Orange, CircleShape).align(Alignment.CenterHorizontally))
            Icon(Icons.Outlined.CheckCircle, "Concluído", tint = Green, modifier = Modifier.size(42.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(body, color = Muted, lineHeight = 21.sp)
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(Orange)) { Text(action, fontWeight = FontWeight.Bold) }
        }
    }
}
