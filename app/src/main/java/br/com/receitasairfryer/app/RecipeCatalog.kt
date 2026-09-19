package br.com.receitasairfryer.app

import java.text.Normalizer

data class CookStep(val instruction: String, val minutes: Int = 0)

enum class DietaryTag {
    SEM_LACTOSE, SEM_GLUTEN, VEGETARIANA, VEGANA, DOCES, FIT,
    @Deprecated("Use SEM_LACTOSE") LACTOSE_FREE,
    @Deprecated("Use SEM_GLUTEN") GLUTEN_FREE,
    @Deprecated("Use VEGETARIANA") VEGETARIAN,
    @Deprecated("Use DOCES") SWEET
}

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val mainIngredient: String,
    val minutes: Int,
    val temperature: Int,
    val servings: Int,
    val calories: Int,
    val difficulty: String,
    val preheat: Boolean,
    val turn: Boolean,
    val ingredients: List<String>,
    val pantryKeys: Set<String>,
    val steps: List<CookStep>,
    val palette: Int,
    val healthy: Boolean = false,
    val imageKey: String? = null,
    val dietaryTags: Set<DietaryTag> = emptySet(),
    val lactoseNotice: Boolean = false,
    val allergens: Set<String> = emptySet()
)

data class GuideItem(val name: String, val cut: String, val temperature: Int, val time: String, val tip: String)

object RecipeCatalog {
    val recipes = listOf(
        recipe("batata-crocante", "Batata crocante", "Dourada por fora, macia no centro e temperada com páprica.", "Batatas", "Batata", 22, 200, 3, 238, true, true,
            listOf("500 g de batata asterix", "1 colher (sopa) de azeite", "1 colher (chá) de páprica doce", "1/2 colher (chá) de sal", "Pimenta-do-reino a gosto"), setOf("batata", "azeite", "paprica"),
            CookStep("Corte as batatas em palitos de mesma espessura e lave até a água sair clara."), CookStep("Seque muito bem, misture com azeite, páprica, sal e pimenta."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Distribua sem amontoar e asse a 200°C.", 10), CookStep("Agite o cesto e asse novamente até dourar.", 9)),
        recipe("frango-crocante", "Frango crocante", "Cubos suculentos com casquinha de parmesão e especiarias.", "Frango", "Frango", 24, 190, 4, 356, true, true,
            listOf("600 g de peito de frango em cubos", "2 colheres (sopa) de iogurte natural", "3 colheres (sopa) de parmesão ralado", "2 colheres (sopa) de farinha panko", "1 dente de alho", "Sal e páprica a gosto"), setOf("frango", "iogurte", "queijo", "alho"),
            CookStep("Misture o frango com iogurte, alho, sal e páprica."), CookStep("Empane levemente com parmesão e farinha panko."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Acomode os cubos sem sobrepor e asse.", 11), CookStep("Vire os cubos e finalize até o centro chegar a 74°C.", 10)),
        recipe("salmao-ervas", "Salmão com ervas", "Filé úmido, cítrico e pronto para um jantar rápido.", "Peixes", "Peixe", 14, 180, 2, 331, false, false,
            listOf("2 filés de salmão de 160 g", "1 colher (chá) de azeite", "Suco e raspas de 1/2 limão", "1 colher (chá) de ervas secas", "Sal e pimenta a gosto"), setOf("peixe", "azeite", "limao"),
            CookStep("Seque os filés e tempere com sal e pimenta."), CookStep("Misture azeite, limão e ervas e espalhe sobre o peixe."), CookStep("Asse com a pele voltada para baixo a 180°C.", 10), CookStep("Confira se lasca facilmente e descanse antes de servir.", 2)),
        recipe("legumes-mediterraneos", "Legumes mediterrâneos", "Mix colorido com bordas tostadas e molho de limão.", "Vegetarianas", "Legumes", 16, 190, 3, 142, true, true,
            listOf("1 abobrinha pequena", "1/2 pimentão vermelho", "1/2 cebola roxa", "150 g de tomate-cereja", "1 colher (sopa) de azeite", "Orégano, sal e limão"), setOf("abobrinha", "tomate", "cebola", "azeite"),
            CookStep("Corte os legumes em tamanhos semelhantes."), CookStep("Misture com azeite, orégano e sal."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Asse os legumes espalhados no cesto.", 7), CookStep("Mexa, junte os tomates e finalize. Tempere com limão.", 6), healthy = true),
        recipe("pao-queijo", "Pão de queijo", "Casquinha firme e miolo elástico, direto do congelador.", "Lanches", "Queijo", 12, 180, 4, 246, true, false,
            listOf("12 pães de queijo congelados (300 g)"), setOf("queijo"),
            CookStep("Preaqueça a Air Fryer a 180°C.", 3), CookStep("Disponha os pães de queijo congelados com espaço entre eles."), CookStep("Asse até crescerem e ficarem dourados.", 9), CookStep("Aguarde 2 minutos antes de servir.")),
        recipe("coxinha-asa", "Coxinha da asa dourada", "Frango bem temperado com pele sequinha e crocante.", "Frango", "Frango", 28, 200, 4, 398, true, true,
            listOf("800 g de coxinha da asa", "1 colher (sopa) de azeite", "1 colher (chá) de páprica defumada", "1 colher (chá) de alho em pó", "Sal, pimenta e limão"), setOf("frango", "azeite", "limao", "alho"),
            CookStep("Seque o frango e tempere com azeite, páprica, alho, sal e pimenta."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Asse em uma única camada.", 12), CookStep("Vire cada pedaço e continue assando.", 11), CookStep("Confirme 74°C junto ao osso e finalize com limão.", 2)),
        recipe("mandioca", "Mandioca dourada", "Pedaços cremosos por dentro, com crosta de alho e ervas.", "Acompanhamentos", "Mandioca", 20, 200, 4, 284, true, true,
            listOf("600 g de mandioca cozida", "1 colher (sopa) de azeite", "1 dente de alho amassado", "Alecrim e sal a gosto"), setOf("mandioca", "azeite", "alho"),
            CookStep("Cozinhe a mandioca até ficar macia, escorra e retire o fio central."), CookStep("Corte, seque e envolva com azeite, alho e sal."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Asse sem sobrepor.", 8), CookStep("Vire e finalize com alecrim.", 9)),
        recipe("hamburguer-caseiro", "Hambúrguer caseiro", "Carne selada, suculenta e com queijo derretido.", "Carnes", "Carne", 15, 200, 2, 412, true, true,
            listOf("360 g de carne moída com 20% de gordura", "2 fatias de queijo", "Sal e pimenta-do-reino", "2 pães de hambúrguer"), setOf("carne", "queijo", "pao"),
            CookStep("Modele dois discos sem compactar demais e tempere por fora."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Asse os hambúrgueres por 5 minutos.", 5), CookStep("Vire, cubra com queijo e asse até o ponto desejado.", 5), CookStep("Descanse por 2 minutos e monte nos pães.", 2)),
        recipe("brocolis-alho", "Brócolis tostado", "Floretes crocantes com alho, limão e parmesão.", "Vegetarianas", "Brócolis", 12, 190, 3, 118, true, true,
            listOf("350 g de brócolis em floretes", "1 colher (sopa) de azeite", "1 dente de alho ralado", "2 colheres (sopa) de parmesão", "Sal e limão"), setOf("brocolis", "azeite", "alho", "queijo"),
            CookStep("Lave e seque completamente os floretes."), CookStep("Misture com azeite, alho e sal."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Asse e agite na metade do tempo.", 7), CookStep("Finalize com parmesão e limão.", 2), healthy = true),
        recipe("banana-canela", "Banana assada com canela", "Sobremesa quente, caramelizada e sem açúcar refinado.", "Doces", "Banana", 10, 180, 2, 167, false, false,
            listOf("2 bananas maduras", "1/2 colher (chá) de canela", "1 colher (chá) de mel", "2 colheres (sopa) de aveia"), setOf("banana", "aveia"),
            CookStep("Corte as bananas ao meio no sentido do comprimento."), CookStep("Polvilhe canela e aveia e regue com mel."), CookStep("Asse sobre papel próprio perfurado a 180°C.", 8), CookStep("Sirva quente após descansar por 1 minuto."), healthy = true),
        recipe("tilapia-limao", "Tilápia ao limão", "Filé leve com cobertura delicada de ervas e alho.", "Peixes", "Peixe", 13, 190, 2, 221, true, false,
            listOf("2 filés de tilápia (320 g)", "1 colher (chá) de azeite", "1/2 limão", "1 dente de alho", "Salsinha, sal e pimenta"), setOf("peixe", "limao", "alho", "azeite"),
            CookStep("Seque a tilápia e tempere com sal, pimenta, alho e limão."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Pincele azeite e asse os filés sem sobrepor.", 8), CookStep("Verifique se o peixe está opaco e finalize com salsinha.", 2), healthy = true),
        recipe("bife-acebolado", "Bife acebolado", "Contrafilé dourado com cebola macia e saborosa.", "Carnes", "Carne", 16, 200, 2, 387, true, true,
            listOf("2 bifes de contrafilé (180 g cada)", "1 cebola em pétalas", "1 colher (chá) de azeite", "Sal, pimenta e molho inglês"), setOf("carne", "cebola", "azeite"),
            CookStep("Retire os bifes da geladeira 15 minutos antes e seque."), CookStep("Tempere a carne e misture a cebola com azeite."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Asse bifes e cebola por 6 minutos.", 6), CookStep("Vire e asse até o ponto desejado. Descanse a carne.", 7)),
        recipe("abobrinha-recheada", "Abobrinha recheada", "Barquinhas leves com carne, tomate e queijo gratinado.", "Fit", "Abobrinha", 22, 180, 2, 294, true, false,
            listOf("2 abobrinhas pequenas", "200 g de carne moída já refogada", "1 tomate sem sementes", "60 g de muçarela", "Sal e orégano"), setOf("abobrinha", "carne", "tomate", "queijo"),
            CookStep("Corte as abobrinhas ao meio e retire parte do miolo."), CookStep("Misture carne, tomate, miolo picado, sal e orégano."), CookStep("Preaqueça a Air Fryer a 180°C.", 3), CookStep("Asse as barquinhas recheadas.", 12), CookStep("Cubra com queijo e gratine.", 5), healthy = true),
        recipe("omelete", "Omelete de forno", "Ovos fofos com tomate, queijo e ervas em porção individual.", "Café da manhã", "Ovo", 12, 170, 1, 276, true, false,
            listOf("2 ovos", "2 colheres (sopa) de leite", "1/2 tomate picado", "30 g de queijo", "Cebolinha, sal e pimenta"), setOf("ovo", "leite", "tomate", "queijo"),
            CookStep("Bata ovos, leite, sal e pimenta apenas até misturar."), CookStep("Junte tomate, queijo e cebolinha em uma forma untada."), CookStep("Preaqueça a Air Fryer a 170°C.", 3), CookStep("Asse até as bordas firmarem e o centro ficar úmido.", 9)),
        recipe("couve-flor", "Couve-flor com curry", "Floretes aromáticos, tostados e levemente picantes.", "Vegetarianas", "Couve-flor", 15, 190, 3, 126, true, true,
            listOf("400 g de couve-flor", "1 colher (sopa) de azeite", "1 colher (chá) de curry", "1/2 colher (chá) de cúrcuma", "Sal e limão"), setOf("couve-flor", "azeite", "limao"),
            CookStep("Separe floretes pequenos, lave e seque bem."), CookStep("Misture com azeite, curry, cúrcuma e sal."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Asse por 6 minutos.", 6), CookStep("Agite o cesto e finalize. Sirva com limão.", 6), healthy = true),
        recipe("linguica-cebola", "Linguiça com cebola", "Gomos dourados com cebola adocicada, sem sujeira no fogão.", "Carnes", "Linguiça", 20, 200, 4, 468, false, true,
            listOf("600 g de linguiça toscana", "2 cebolas em pétalas", "1 colher (chá) de mostarda", "Pimenta-do-reino a gosto"), setOf("linguica", "cebola"),
            CookStep("Faça pequenos furos apenas na película e acomode os gomos."), CookStep("Asse a 200°C por 8 minutos.", 8), CookStep("Vire, adicione cebola misturada com mostarda e pimenta."), CookStep("Asse até dourar e atingir 71°C no centro.", 10), CookStep("Descanse 2 minutos antes de cortar.", 2)),
        recipe("quibe", "Quibe assado", "Quibe úmido com hortelã e superfície bem tostada.", "Lanches", "Carne", 20, 190, 4, 318, true, true,
            listOf("300 g de carne moída", "150 g de trigo para quibe hidratado", "1/2 cebola ralada", "2 colheres (sopa) de hortelã", "Sal, pimenta síria e azeite"), setOf("carne", "cebola", "trigo"),
            CookStep("Esprema bem o trigo hidratado."), CookStep("Misture carne, trigo, cebola, hortelã e temperos."), CookStep("Modele quibes pequenos e pincele azeite."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Asse, vire na metade e confirme 71°C no centro.", 15)),
        recipe("torrada-caprese", "Torrada caprese", "Pão crocante, tomate suculento e queijo derretido.", "Lanches", "Pão", 8, 180, 2, 263, false, false,
            listOf("4 fatias de pão italiano", "1 tomate fatiado", "100 g de muçarela", "Folhas de manjericão", "Azeite, sal e pimenta"), setOf("pao", "tomate", "queijo", "azeite"),
            CookStep("Regue o pão com um fio de azeite."), CookStep("Cubra com tomate temperado e muçarela."), CookStep("Asse a 180°C até o queijo derreter e a base dourar.", 6), CookStep("Finalize com manjericão e pimenta.")),
        recipe("maca-crumble", "Maçã com crumble", "Maçã macia sob uma farofa crocante de aveia e canela.", "Doces", "Maçã", 18, 180, 2, 219, true, false,
            listOf("2 maçãs", "3 colheres (sopa) de aveia", "1 colher (sopa) de manteiga", "1 colher (sopa) de açúcar mascavo", "Canela a gosto"), setOf("maca", "aveia", "manteiga"),
            CookStep("Pique as maçãs e distribua em dois ramequins."), CookStep("Misture aveia, manteiga, açúcar e canela formando uma farofa."), CookStep("Cubra as maçãs e preaqueça a Air Fryer a 180°C.", 3), CookStep("Asse até borbulhar e dourar.", 15)),
        recipe("grao-bico", "Grão-de-bico crocante", "Petisco rico em fibras, seco e intensamente temperado.", "Fit", "Grão-de-bico", 18, 200, 4, 184, true, true,
            listOf("400 g de grão-de-bico cozido e escorrido", "1 colher (chá) de azeite", "1 colher (chá) de páprica", "1/2 colher (chá) de cominho", "Sal a gosto"), setOf("grao-de-bico", "azeite", "paprica"),
            CookStep("Escorra, lave e seque muito bem o grão-de-bico."), CookStep("Misture com azeite, páprica, cominho e sal."), CookStep("Preaqueça a Air Fryer a 200°C.", 3), CookStep("Asse em camada uniforme e agite a cada 5 minutos.", 15), CookStep("Espere esfriar para ficar ainda mais crocante."), healthy = true),
        recipe("costelinha", "Costelinha barbecue", "Pedaços macios, laqueados e caramelizados no ponto.", "Carnes", "Carne", 32, 180, 4, 526, true, true,
            listOf("800 g de costelinha suína em ripas", "3 colheres (sopa) de molho barbecue", "1 colher (chá) de páprica", "1 dente de alho", "Sal e pimenta"), setOf("carne", "alho"),
            CookStep("Tempere a costelinha com alho, páprica, sal e pimenta."), CookStep("Preaqueça a Air Fryer a 180°C.", 3), CookStep("Asse as ripas sem sobrepor.", 13), CookStep("Vire e asse por mais 10 minutos.", 10), CookStep("Pincele barbecue dos dois lados e caramelize, sem deixar queimar.", 6)),
        recipe("empanado-berinjela", "Berinjela empanada", "Rodelas sequinhas com parmesão, ótimas como entrada.", "Vegetarianas", "Berinjela", 17, 190, 3, 198, true, true,
            listOf("1 berinjela grande", "1 ovo", "4 colheres (sopa) de farinha panko", "3 colheres (sopa) de parmesão", "Sal, pimenta e azeite em spray"), setOf("berinjela", "ovo", "queijo"),
            CookStep("Corte a berinjela em rodelas, salgue e seque após 10 minutos."), CookStep("Passe no ovo e depois na mistura de panko e parmesão."), CookStep("Preaqueça a Air Fryer a 190°C.", 3), CookStep("Borrife azeite e asse por 6 minutos.", 6), CookStep("Vire, borrife novamente e finalize.", 6), healthy = true, dietary = setOf(DietaryTag.VEGETARIANA))
        , recipe("brownie-cacau", "Brownie de cacau", "Quadradinhos úmidos por dentro e com casquinha delicada.", "Doces", "Cacau", 18, 160, 6, 238, true, false,
            listOf("80 g de chocolate meio amargo picado", "60 g de manteiga", "1/2 xícara de açúcar", "1 ovo", "1/3 xícara de farinha de trigo", "2 colheres (sopa) de cacau em pó", "1 pitada de sal"), setOf("chocolate", "manteiga", "ovo"),
            CookStep("Forre uma forma pequena que caiba no cesto."), CookStep("Derreta chocolate e manteiga, misture açúcar e ovo."), CookStep("Incorpore farinha, cacau e sal sem bater demais."), CookStep("Preaqueça a Air Fryer a 160°C.", 3), CookStep("Asse até as bordas firmarem e o centro ainda estar úmido.", 15), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("cookie-aveia", "Cookie de aveia e gotas", "Cookie macio no centro, com bordas douradas.", "Doces", "Aveia", 14, 170, 8, 156, true, false,
            listOf("1 xícara de aveia em flocos", "1/2 xícara de farinha de trigo", "1/3 xícara de açúcar mascavo", "60 g de manteiga", "1 ovo", "1/3 xícara de gotas de chocolate", "1/2 colher (chá) de fermento"), setOf("aveia", "farinha", "manteiga", "ovo", "chocolate"),
            CookStep("Misture manteiga, açúcar e ovo."), CookStep("Junte aveia, farinha, fermento e gotas de chocolate."), CookStep("Modele bolas e achate em papel próprio perfurado."), CookStep("Asse a 170°C por 10 minutos, em lotes, até dourar as bordas.", 10), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("pudim-leite", "Pudim de leite condensado", "Pudim cremoso em ramequins, com calda dourada.", "Doces", "Leite", 25, 160, 4, 286, false, false,
            listOf("1/2 xícara de açúcar para a calda", "1/2 lata de leite condensado", "1/2 xícara de leite", "2 ovos"), setOf("leite", "ovo"),
            CookStep("Derreta o açúcar até formar caramelo e distribua em quatro ramequins."), CookStep("Bata leite condensado, leite e ovos apenas até misturar."), CookStep("Despeje nos ramequins e cubra com papel-alumínio."), CookStep("Asse a 160°C até firmar no centro.", 20), CookStep("Esfrie antes de desenformar."), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("pasteis-banana", "Pastelzinho de banana", "Massa fina e crocante com banana e canela.", "Doces", "Banana", 12, 180, 6, 142, true, false,
            listOf("6 discos de massa para pastel", "2 bananas maduras em rodelas", "1 colher (sopa) de açúcar", "1/2 colher (chá) de canela", "Água para fechar"), setOf("banana", "canela"),
            CookStep("Misture banana, açúcar e canela."), CookStep("Recheie os discos, umedeça as bordas e pressione com garfo."), CookStep("Pincele levemente com óleo."), CookStep("Asse a 180°C, virando na metade, até dourar.", 10), dietary = setOf(DietaryTag.SWEET))
        , recipe("cheesecake-goiabada", "Cheesecake de goiabada", "Mini cheesecake assado com cobertura brilhante de goiabada.", "Doces", "Goiabada", 24, 160, 4, 302, true, false,
            listOf("100 g de biscoito maisena triturado", "35 g de manteiga derretida", "200 g de cream cheese", "1 ovo", "2 colheres (sopa) de açúcar", "80 g de goiabada", "2 colheres (sopa) de água"), setOf("biscoito", "manteiga", "queijo", "ovo", "goiabada"),
            CookStep("Misture biscoito e manteiga e pressione em quatro formas."), CookStep("Bata cream cheese, açúcar e ovo até ficar homogêneo."), CookStep("Distribua sobre a base."), CookStep("Asse a 160°C até o centro firmar.", 18), CookStep("Derreta goiabada com água e cubra após esfriar."), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("coco-queimado", "Cocada de forno", "Cocada dourada nas bordas e cremosa no centro.", "Doces", "Coco", 16, 170, 6, 196, false, false,
            listOf("2 xícaras de coco ralado sem açúcar", "1/2 lata de leite condensado", "1 ovo", "1 colher (sopa) de manteiga", "1 pitada de sal"), setOf("coco", "leite", "ovo", "manteiga"),
            CookStep("Misture todos os ingredientes até formar uma massa úmida."), CookStep("Distribua em forminhas untadas, sem compactar."), CookStep("Asse a 170°C até dourar as pontas.", 13), CookStep("Descanse 5 minutos antes de servir."), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("pera-mel-canela", "Pera assada com canela", "Pera macia, perfumada e finalizada com nozes crocantes.", "Doces", "Pera", 15, 180, 2, 174, false, false,
            listOf("2 peras firmes", "1 colher (sopa) de mel", "1/2 colher (chá) de canela", "2 colheres (sopa) de nozes picadas", "1 colher (chá) de suco de limão"), setOf("pera", "mel", "canela", "nozes"),
            CookStep("Corte as peras ao meio e retire as sementes."), CookStep("Pincele limão, mel e canela."), CookStep("Asse com o corte para cima a 180°C.", 11), CookStep("Finalize com nozes e sirva morna.", 2), dietary = setOf(DietaryTag.SWEET, DietaryTag.LACTOSE_FREE, DietaryTag.GLUTEN_FREE, DietaryTag.VEGETARIAN))
        , recipe("churros-canela", "Churros de canela", "Palitos dourados para finalizar com açúcar e canela.", "Doces", "Farinha", 20, 190, 4, 224, true, false,
            listOf("1 xícara de água", "1 colher (sopa) de manteiga", "1 colher (sopa) de açúcar", "1 xícara de farinha de trigo", "1 ovo", "Açúcar e canela para finalizar"), setOf("farinha", "manteiga", "ovo"),
            CookStep("Ferva água, manteiga e açúcar; junte a farinha e mexa até soltar da panela."), CookStep("Esfrie por 5 minutos e incorpore o ovo."), CookStep("Modele tiras com saco de confeitar e pincele óleo."), CookStep("Asse a 190°C, virando na metade, até dourar.", 16), CookStep("Passe em açúcar e canela."), dietary = setOf(DietaryTag.SWEET), lactoseNotice = true)
        , recipe("muffin-maca", "Muffin de maçã", "Bolinho individual de maçã com canela e miolo macio.", "Doces", "Maçã", 18, 170, 6, 205, true, false,
            listOf("1 maçã picada", "1 ovo", "1/3 xícara de óleo", "1/2 xícara de açúcar", "3/4 xícara de farinha de trigo", "1/2 colher (chá) de canela", "1/2 colher (chá) de fermento"), setOf("maca", "ovo", "farinha"),
            CookStep("Misture ovo, óleo e açúcar."), CookStep("Junte farinha, canela e fermento; incorpore a maçã."), CookStep("Divida em seis forminhas, preenchendo até dois terços."), CookStep("Asse a 170°C até dourar e o palito sair limpo.", 15), dietary = setOf(DietaryTag.SWEET))
    )

    val guide = listOf(
        GuideItem("Batata palito", "Palitos de 1 cm", 200, "15–22 min", "Lave, seque e agite duas vezes."),
        GuideItem("Batata congelada", "Direto do freezer", 200, "12–18 min", "Não descongele; evite encher o cesto."),
        GuideItem("Peito de frango", "Cubos de 3 cm", 190, "16–22 min", "Vire e confirme 74°C no centro."),
        GuideItem("Coxa de frango", "Com osso", 200, "22–30 min", "Comece com a pele para baixo e vire."),
        GuideItem("Bife bovino", "2 cm de espessura", 200, "8–14 min", "Vire uma vez e descanse antes de cortar."),
        GuideItem("Hambúrguer", "Disco de 180 g", 200, "10–14 min", "Não pressione durante o preparo."),
        GuideItem("Linguiça", "Gomos inteiros", 200, "15–20 min", "Vire na metade; centro a 71°C."),
        GuideItem("Salmão", "Filé de 3 cm", 180, "8–12 min", "Asse com a pele voltada para baixo."),
        GuideItem("Tilápia", "Filé descongelado", 190, "8–11 min", "Pincele azeite para não ressecar."),
        GuideItem("Brócolis", "Floretes médios", 190, "7–10 min", "Seque bem e agite na metade."),
        GuideItem("Couve-flor", "Floretes médios", 190, "10–14 min", "Deixe espaço para o ar circular."),
        GuideItem("Abobrinha", "Meias-luas", 190, "8–12 min", "Salgue somente antes de assar."),
        GuideItem("Pão de queijo", "Congelado", 180, "8–12 min", "Mantenha espaço para crescer."),
        GuideItem("Mandioca", "Cozida em pedaços", 200, "14–20 min", "Pincele azeite e vire na metade."),
        GuideItem("Legumes variados", "Cubos de 2 cm", 190, "12–18 min", "Coloque os mais firmes primeiro.")
    )

    fun search(query: String, category: String? = null, quickOnly: Boolean = false, healthyOnly: Boolean = false, dietary: Set<DietaryTag> = emptySet()): List<Recipe> {
        val needle = query.normalized()
        return recipes.filter { recipe ->
            val text = (recipe.name + " " + recipe.category + " " + recipe.mainIngredient + " " + recipe.ingredients.joinToString()).normalized()
            (needle.isBlank() || needle in text) && (category == null || recipe.category == category) && (!quickOnly || recipe.minutes <= 15) && (!healthyOnly || recipe.healthy) && dietary.all { tag -> recipe.dietaryTags.any { it == tag || (tag == DietaryTag.DOCES && it == DietaryTag.SWEET) || (tag == DietaryTag.SEM_LACTOSE && it == DietaryTag.LACTOSE_FREE) || (tag == DietaryTag.SEM_GLUTEN && it == DietaryTag.GLUTEN_FREE) || (tag == DietaryTag.VEGETARIANA && it == DietaryTag.VEGETARIAN) } }
        }
    }

    fun rankedByPantry(selected: Set<String>): List<Pair<Recipe, Int>> = recipes.map { recipe ->
        recipe to recipe.pantryKeys.count { it in selected.map(String::normalized) }
    }.sortedWith(compareByDescending<Pair<Recipe, Int>> { it.second }.thenBy { it.first.minutes }.thenBy { it.first.name })

    private fun recipe(id: String, name: String, description: String, category: String, main: String, minutes: Int, temperature: Int, servings: Int, calories: Int, preheat: Boolean, turn: Boolean, ingredients: List<String>, pantry: Set<String>, vararg steps: CookStep, healthy: Boolean = false, dietary: Set<DietaryTag> = defaultDietary(category), lactoseNotice: Boolean = false) =
        Recipe(id, name, description, category, main, minutes, temperature, servings, calories, if (minutes <= 18) "Fácil" else "Intermediário", preheat, turn, ingredients, pantry.map(String::normalized).toSet(), steps.toList(), id.hashCode(), healthy, imageKeyFor(id), normalizeTags(dietary), lactoseNotice, allergensFor(ingredients))

    private fun defaultDietary(category: String): Set<DietaryTag> = when (category) {
        "Doces" -> setOf(DietaryTag.DOCES)
        "Vegetarianas" -> setOf(DietaryTag.VEGETARIANA)
        else -> emptySet()
    }

    private fun allergensFor(ingredients: List<String>): Set<String> {
        val text = ingredients.joinToString(" ").normalized()
        return buildSet {
            if (listOf("leite", "manteiga", "queijo", "cream cheese", "parmesao").any { it in text }) add("LEITE")
            if (listOf("farinha", "trigo", "pao", "biscoito", "aveia").any { it in text }) add("GLUTEN")
            if ("ovo" in text) add("OVO")
            if (listOf("nozes", "castanha", "amendoim").any { it in text }) add("OLEAGINOSAS")
        }
    }

    private fun normalizeTags(tags: Set<DietaryTag>): Set<DietaryTag> = tags.map {
        when (it) {
            DietaryTag.LACTOSE_FREE -> DietaryTag.SEM_LACTOSE
            DietaryTag.GLUTEN_FREE -> DietaryTag.SEM_GLUTEN
            DietaryTag.VEGETARIAN -> DietaryTag.VEGETARIANA
            DietaryTag.SWEET -> DietaryTag.DOCES
            else -> it
        }
    }.toSet()

    private fun imageKeyFor(id: String): String? = when (id) {
        "batata-crocante" -> "asset_batata_crocante"
        "frango-crocante" -> "asset_frango_crocante"
        "salmao-ervas" -> "asset_salmao_ervas"
        "hamburguer-caseiro" -> "asset_hamburguer_caseiro"
        "brocolis-alho" -> "asset_brocolis_alho"
        "banana-canela" -> "asset_banana_canela"
        "legumes-mediterraneos" -> "asset_legumes_mediterraneos"
        "pao-queijo" -> "asset_pao_queijo"
        "coxinha-asa" -> "asset_coxinha_asa"
        "mandioca" -> "asset_mandioca"
        "bife-acebolado" -> "asset_bife_acebolado"
        "omelete" -> "asset_omelete"
        "couve-flor" -> "asset_couve_flor"
        "linguica-cebola" -> "asset_linguica_cebola"
        "quibe" -> "asset_quibe"
        "maca-crumble" -> "asset_maca_crumble"
        else -> null
    }
}

fun String.normalized(): String = Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD).replace("\\p{Mn}+".toRegex(), "")

fun remainingSeconds(endTimestamp: Long, nowTimestamp: Long): Int = ((endTimestamp - nowTimestamp).coerceAtLeast(0L) / 1000L).toInt()
