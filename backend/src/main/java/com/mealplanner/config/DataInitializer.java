package com.mealplanner.config;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.MealType;
import com.mealplanner.domain.Recipe;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.RecipeStep;
import com.mealplanner.repository.IngredientRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.RecipeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    private static final String IMAGE_PLACEHOLDER = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=900&q=80";

    @Bean
    @Transactional
    @ConditionalOnProperty(name = "meal-planner.seed.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedData(
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository,
            MealPlanRepository mealPlanRepository
    ) {
        return args -> {
            seedDetailedRecipes(recipeRepository, ingredientRepository);
            seedDishIdeas(recipeRepository);
            seedSampleMealPlan(recipeRepository, mealPlanRepository);
        };
    }

    private void seedDetailedRecipes(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        Recipe carbonara = saveRecipeIfMissing(recipeRepository,
                "Pates carbonara",
                "Une recette rapide pour les soirs de semaine.",
                4,
                10,
                15,
                "Plats principaux",
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=900&q=80",
                Set.of("pâtes", "facile", "moins de 30 minutes"),
                List.of(
                        ingredientData(ingredientRepository, "Pates", "Epicerie salée", "g", "400"),
                        ingredientData(ingredientRepository, "Lardons", "Viandes et poissons", "g", "200"),
                        ingredientData(ingredientRepository, "Oeufs", "Frais", "pièce", "3"),
                        ingredientData(ingredientRepository, "Parmesan", "Frais", "g", "80")
                ),
                "Faire cuire les pâtes dans une grande casserole d'eau bouillante salée.",
                "Faire revenir les lardons pendant la cuisson des pâtes.",
                "Mélanger les oeufs avec le parmesan râpé.",
                "Ajouter les pâtes chaudes et mélanger rapidement hors du feu.");

        Recipe curry = saveRecipeIfMissing(recipeRepository,
                "Poulet curry coco",
                "Un plat complet, doux et parfumé.",
                4,
                15,
                25,
                "Plats principaux",
                "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=900&q=80",
                Set.of("poulet", "riz", "batch cooking"),
                List.of(
                        ingredientData(ingredientRepository, "Poulet", "Viandes et poissons", "g", "500"),
                        ingredientData(ingredientRepository, "Riz", "Epicerie salée", "g", "300"),
                        ingredientData(ingredientRepository, "Lait de coco", "Epicerie salée", "ml", "400")
                ),
                "Couper le poulet en morceaux réguliers.",
                "Faire dorer le poulet avec une cuillère d'huile.",
                "Ajouter le curry puis le lait de coco.",
                "Laisser mijoter et servir avec le riz.");

        Recipe salad = saveRecipeIfMissing(recipeRepository,
                "Salade pois chiches tomate",
                "Fraîche, économique et facile à préparer.",
                2,
                12,
                0,
                "Végétarien",
                "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=900&q=80",
                Set.of("healthy", "économique", "végétarien"),
                List.of(
                        ingredientData(ingredientRepository, "Pois chiches", "Epicerie salée", "g", "300"),
                        ingredientData(ingredientRepository, "Tomates", "Fruits et légumes", "pièce", "4"),
                        ingredientData(ingredientRepository, "Concombre", "Fruits et légumes", "pièce", "1")
                ),
                "Rincer et égoutter les pois chiches.",
                "Couper les tomates et le concombre.",
                "Mélanger avec une vinaigrette simple et servir frais.");

        saveRecipeIfMissing(recipeRepository,
                "Bolognaise de légumes",
                "Sauce légumes tomate servie avec des spaghettis, burrata et parmesan.",
                4,
                20,
                35,
                "Pâtes",
                IMAGE_PLACEHOLDER,
                Set.of("pâtes", "légumes", "burrata"),
                List.of(
                        ingredientData(ingredientRepository, "Oignons", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Carottes", "Fruits et légumes", "pièce", "3"),
                        ingredientData(ingredientRepository, "Courge", "Fruits et légumes", "g", "500"),
                        ingredientData(ingredientRepository, "Sauce tomate", "Epicerie salée", "g", "500"),
                        ingredientData(ingredientRepository, "Spaghettis", "Epicerie salée", "g", "400"),
                        ingredientData(ingredientRepository, "Burrata", "Frais", "pièce", "1"),
                        ingredientData(ingredientRepository, "Parmesan", "Frais", "g", "60"),
                        ingredientData(ingredientRepository, "Persil", "Fruits et légumes", "botte", "1")
                ),
                "Faire revenir les oignons avec de l'huile de pépin de raisin.",
                "Ajouter les carottes coupées en carrés et laisser commencer à cuire.",
                "Ajouter la courge coupée en carrés, saler, poivrer et cuire à feu doux avec un couvercle.",
                "Faire cuire les spaghettis.",
                "Ajouter la sauce tomate et le persil dans les légumes.",
                "Servir les pâtes avec les légumes, la burrata et du parmesan si souhaité.");

        saveRecipeIfMissing(recipeRepository,
                "Pinsa courge Butternut / crème de Burrata / magrets de canard séchés au poivre",
                "Pinsa garnie de courge rôtie, crème de burrata, magret séché et citron.",
                4,
                20,
                60,
                "Pinsa",
                IMAGE_PLACEHOLDER,
                Set.of("pinsa", "courge", "canard"),
                List.of(
                        ingredientData(ingredientRepository, "Pinsa", "Boulangerie", "pièce", "2"),
                        ingredientData(ingredientRepository, "Courge butternut", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Crème de burrata", "Frais", "g", "200"),
                        ingredientData(ingredientRepository, "Magret de canard séché au poivre", "Viandes et poissons", "g", "120"),
                        ingredientData(ingredientRepository, "Mozzarella", "Frais", "g", "150"),
                        ingredientData(ingredientRepository, "Oignon", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Burrata", "Frais", "pièce", "2"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1")
                ),
                "Couper la courge en long, l'évider puis l'assaisonner avec sel, poivre, huile d'olive, citron et thym.",
                "Cuire la courge 45 minutes au four à 200°C.",
                "Étaler la crème de burrata sur les pinsas.",
                "Écraser la courge et l'étaler sur les pinsas.",
                "Ajouter le magret de canard séché, la mozzarella en cubes et l'oignon revenu à la poêle.",
                "Ajouter une burrata ouverte par pinsa et râper le zeste de citron dessus.",
                "Cuire 17 minutes à 180°C.");

        saveRecipeIfMissing(recipeRepository,
                "Pinsa courgettes grillées / pesto / parmesan / pignons",
                "Pinsa au pesto, courgettes grillées, parmesan, pignons et burrata selon envie.",
                4,
                20,
                30,
                "Pinsa",
                IMAGE_PLACEHOLDER,
                Set.of("pinsa", "courgettes", "pesto"),
                List.of(
                        ingredientData(ingredientRepository, "Pinsa", "Boulangerie", "pièce", "2"),
                        ingredientData(ingredientRepository, "Courgettes", "Fruits et légumes", "pièce", "3"),
                        ingredientData(ingredientRepository, "Pesto", "Epicerie salée", "g", "150"),
                        ingredientData(ingredientRepository, "Mozzarella râpée", "Frais", "g", "150"),
                        ingredientData(ingredientRepository, "Parmesan", "Frais", "g", "80"),
                        ingredientData(ingredientRepository, "Pignons de pin", "Epicerie salée", "g", "50"),
                        ingredientData(ingredientRepository, "Burrata", "Frais", "pièce", "2"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1")
                ),
                "Couper les courgettes en longueur.",
                "Les poser sur une plaque, saler, poivrer, ajouter l'huile d'olive au basilic et cuire 10 à 15 minutes à 180°C.",
                "Étaler le pesto sur les pinsas puis ajouter les lamelles de courgettes.",
                "Ajouter mozzarella râpée, parmesan et pignons sur une pinsa.",
                "Sur l'autre, ajouter parmesan, pignons, burrata ouverte et zeste de citron.",
                "Cuire 15 à 16 minutes à 180°C.");

        saveRecipeIfMissing(recipeRepository,
                "Wraps froids poulet avocat tomate crudités fromage frais",
                "Wraps frais au poulet, avocat, tomates, crudités et fromage frais.",
                4,
                25,
                10,
                "Wraps",
                IMAGE_PLACEHOLDER,
                Set.of("wrap", "froid", "poulet"),
                List.of(
                        ingredientData(ingredientRepository, "Filets de poulet", "Viandes et poissons", "pièce", "2"),
                        ingredientData(ingredientRepository, "Galettes de blé", "Boulangerie", "pièce", "4"),
                        ingredientData(ingredientRepository, "Avocat", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Tomates", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Carotte", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Salade", "Fruits et légumes", "poignée", "1"),
                        ingredientData(ingredientRepository, "Fromage frais", "Frais", "g", "100"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1")
                ),
                "Cuire le poulet à la poêle ou au grill avec sel, poivre et herbes, puis le laisser refroidir et le couper en lamelles.",
                "Couper les tomates en dés, l'avocat en lamelles et citronner l'avocat.",
                "Tartiner chaque wrap de fromage frais.",
                "Ajouter salade, carottes râpées, avocat, tomates et poulet.",
                "Rouler serré, réserver au frais 20 à 30 minutes puis couper en deux.");

        saveRecipeIfMissing(recipeRepository,
                "Gnocchis poêlés / courgettes / citron / parmesan",
                "Gnocchis croustillants aux courgettes, citron et parmesan.",
                2,
                10,
                18,
                "Végétarien",
                IMAGE_PLACEHOLDER,
                Set.of("gnocchis", "courgettes", "rapide"),
                List.of(
                        ingredientData(ingredientRepository, "Gnocchis frais", "Frais", "g", "400"),
                        ingredientData(ingredientRepository, "Courgettes", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Parmesan", "Frais", "g", "50"),
                        ingredientData(ingredientRepository, "Ail", "Fruits et légumes", "gousse", "1")
                ),
                "Laver et couper les courgettes en dés ou demi-rondelles.",
                "Faire revenir les courgettes dans l'huile d'olive avec l'ail pendant 5 à 8 minutes.",
                "Ajouter les gnocchis directement dans la poêle et les faire dorer 10 à 12 minutes.",
                "Zester le citron puis ajouter un filet de jus.",
                "Terminer avec parmesan, sel, poivre et basilic ou persil.");

        saveRecipeIfMissing(recipeRepository,
                "Shakshuka",
                "Oeufs pochés dans une sauce tomate aux poivrons et épices.",
                2,
                10,
                25,
                "Végétarien",
                IMAGE_PLACEHOLDER,
                Set.of("oeufs", "tomate", "poivrons"),
                List.of(
                        ingredientData(ingredientRepository, "Oeufs", "Frais", "pièce", "4"),
                        ingredientData(ingredientRepository, "Tomates concassées", "Epicerie salée", "g", "400"),
                        ingredientData(ingredientRepository, "Poivron rouge", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Oignon", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Ail", "Fruits et légumes", "gousse", "1"),
                        ingredientData(ingredientRepository, "Cumin", "Epices", "cuillère", "1"),
                        ingredientData(ingredientRepository, "Paprika", "Epices", "cuillère", "1")
                ),
                "Émincer l'oignon, le poivron et l'ail puis les faire revenir dans une poêle avec de l'huile d'olive.",
                "Ajouter cumin et paprika puis verser les tomates concassées.",
                "Saler, poivrer et laisser mijoter 10 à 15 minutes.",
                "Faire quatre creux dans la sauce et casser les oeufs.",
                "Couvrir et cuire 6 à 8 minutes jusqu'à ce que le blanc soit pris.");

        saveRecipeIfMissing(recipeRepository,
                "Pâtes aux crevettes / sauce soja / légumes pour wok",
                "Crevettes, légumes wok, sauce soja et spaghettis.",
                4,
                10,
                20,
                "Pâtes",
                IMAGE_PLACEHOLDER,
                Set.of("crevettes", "wok", "pâtes"),
                List.of(
                        ingredientData(ingredientRepository, "Crevettes", "Viandes et poissons", "g", "400"),
                        ingredientData(ingredientRepository, "Légumes pour wok", "Surgelés", "g", "600"),
                        ingredientData(ingredientRepository, "Sauce soja", "Epicerie salée", "ml", "80"),
                        ingredientData(ingredientRepository, "Spaghettis", "Epicerie salée", "g", "400"),
                        ingredientData(ingredientRepository, "Huile de sésame", "Epicerie salée", "ml", "30")
                ),
                "Faire cuire les légumes wok dans une poêle avec huile de sésame ou huile pour wok.",
                "Ajouter de la sauce soja pendant la cuisson.",
                "Quand les légumes sont prêts, ajouter les crevettes.",
                "Faire cuire les spaghettis avec un filet d'huile de pépin de raisin.",
                "Mélanger les spaghettis avec les crevettes et les légumes.");

        saveRecipeIfMissing(recipeRepository,
                "Tressé feuilletée saumon / poireaux à la crème",
                "Pâte feuilletée tressée au saumon, poireaux, citron et crème.",
                4,
                20,
                30,
                "Feuilleté",
                IMAGE_PLACEHOLDER,
                Set.of("saumon", "poireaux", "feuilleté"),
                List.of(
                        ingredientData(ingredientRepository, "Pâte feuilletée", "Frais", "pièce", "1"),
                        ingredientData(ingredientRepository, "Saumon", "Viandes et poissons", "g", "400"),
                        ingredientData(ingredientRepository, "Poireaux", "Fruits et légumes", "pièce", "3"),
                        ingredientData(ingredientRepository, "Crème fraîche", "Frais", "g", "150"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Jaune d'oeuf", "Frais", "pièce", "1"),
                        ingredientData(ingredientRepository, "Lait", "Frais", "ml", "30")
                ),
                "Couper les poireaux et les cuire à feu doux avec sel et poivre.",
                "Ajouter de la crème fraîche en fin de cuisson.",
                "Placer le saumon au centre de la pâte feuilletée, saler, poivrer et citronner.",
                "Ajouter les poireaux à la crème au-dessus du saumon.",
                "Découper des bandes de chaque côté de la pâte puis les croiser pour former une tresse.",
                "Dorer avec un jaune d'oeuf mélangé à un peu de lait.",
                "Cuire au four jusqu'à ce que la pâte soit dorée.");

        saveRecipeIfMissing(recipeRepository,
                "Pokebowl",
                "Bol de riz à sushi, saumon, avocat, concombre et carottes assaisonnées.",
                4,
                35,
                15,
                "Bowls",
                IMAGE_PLACEHOLDER,
                Set.of("saumon", "riz", "frais"),
                List.of(
                        ingredientData(ingredientRepository, "Riz à sushi", "Epicerie salée", "g", "600"),
                        ingredientData(ingredientRepository, "Pavé de saumon", "Viandes et poissons", "g", "400"),
                        ingredientData(ingredientRepository, "Avocat", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Concombre", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Carottes râpées", "Fruits et légumes", "g", "200"),
                        ingredientData(ingredientRepository, "Vinaigre de riz", "Epicerie salée", "ml", "150"),
                        ingredientData(ingredientRepository, "Sauce soja", "Epicerie salée", "ml", "80"),
                        ingredientData(ingredientRepository, "Huile de sésame", "Epicerie salée", "ml", "40"),
                        ingredientData(ingredientRepository, "Citron vert", "Fruits et légumes", "pièce", "2")
                ),
                "Cuire le riz à sushi avec 1,2 volume d'eau, puis laisser reposer à couvert 10 minutes.",
                "Aérer le riz et incorporer le mélange vinaigre de riz, sel et sucre.",
                "Assaisonner le saumon avec citron vert et huile de sésame.",
                "Assaisonner carottes et concombre avec sauce à assaisonner et huile de sésame.",
                "Monter les bols avec riz, saumon, avocat, concombre et carottes.");

        saveRecipeIfMissing(recipeRepository,
                "Pita garnie façon méditerranéenne",
                "Pita garnie au poulet mariné, crudités et sauce yaourt grec.",
                4,
                25,
                10,
                "Sandwichs",
                IMAGE_PLACEHOLDER,
                Set.of("pita", "poulet", "méditerranéen"),
                List.of(
                        ingredientData(ingredientRepository, "Pain pita", "Boulangerie", "pièce", "4"),
                        ingredientData(ingredientRepository, "Poulet émincé", "Viandes et poissons", "g", "500"),
                        ingredientData(ingredientRepository, "Salade", "Fruits et légumes", "poignée", "2"),
                        ingredientData(ingredientRepository, "Tomates", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Oignon rouge", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Yaourt grec", "Frais", "g", "200"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Miel", "Epicerie sucrée", "cuillère", "1")
                ),
                "Mariner le poulet avec huile d'olive, sel, poivre, origan, paprika et citron.",
                "Cuire le poulet à feu moyen-vif 8 à 10 minutes.",
                "Mélanger yaourt grec, cumin, curry, miel, citron, sel, poivre et huile d'olive.",
                "Réchauffer les pains pita.",
                "Garnir avec salade, tomates, poulet chaud et sauce au yaourt.");

        saveRecipeIfMissing(recipeRepository,
                "Émincé de dinde aux épices et salade de semoule",
                "Dinde marinée aux épices, servie avec semoule et crudités.",
                4,
                20,
                12,
                "Plats principaux",
                IMAGE_PLACEHOLDER,
                Set.of("dinde", "semoule", "épicé"),
                List.of(
                        ingredientData(ingredientRepository, "Filets de dinde", "Viandes et poissons", "g", "500"),
                        ingredientData(ingredientRepository, "Semoule", "Epicerie salée", "g", "300"),
                        ingredientData(ingredientRepository, "Carottes râpées", "Fruits et légumes", "g", "200"),
                        ingredientData(ingredientRepository, "Concombre", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Tomates", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Persil", "Fruits et légumes", "botte", "1"),
                        ingredientData(ingredientRepository, "Curcuma", "Epices", "cuillère", "1"),
                        ingredientData(ingredientRepository, "Cumin", "Epices", "cuillère", "1"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1")
                ),
                "Mariner la dinde avec curcuma, cumin, citron, sel et poivre.",
                "Poêler la dinde jusqu'à cuisson complète.",
                "Préparer la semoule.",
                "Mélanger semoule, carottes râpées, concombre, tomate et persil.",
                "Servir la dinde chaude avec la salade de semoule.");

        saveRecipeIfMissing(recipeRepository,
                "Chili Con Carne",
                "Chili à la viande hachée, poivron rouge, haricots rouges, tomate et chocolat noir.",
                4,
                20,
                45,
                "Plats mijotés",
                IMAGE_PLACEHOLDER,
                Set.of("boeuf", "chili", "mijoté"),
                List.of(
                        ingredientData(ingredientRepository, "Viande hachée", "Viandes et poissons", "g", "600"),
                        ingredientData(ingredientRepository, "Oignons", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Poivron rouge", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Haricots rouges", "Epicerie salée", "boîte", "1"),
                        ingredientData(ingredientRepository, "Tomates en dés", "Epicerie salée", "boîte", "1"),
                        ingredientData(ingredientRepository, "Epices chili", "Epices", "cuillère", "2"),
                        ingredientData(ingredientRepository, "Chocolat noir", "Epicerie sucrée", "barre", "1")
                ),
                "Faire cuire les oignons avec de l'huile.",
                "Couper le poivron rouge et la viande hachée.",
                "Ajouter les épices chili avant et après la viande, saler et poivrer.",
                "Rincer les haricots rouges.",
                "Ajouter les tomates en dés quand la viande est presque cuite.",
                "Ajouter les haricots rouges puis une barre de chocolat noir.",
                "Laisser cuire à couvert.");

        saveRecipeIfMissing(recipeRepository,
                "Poulet au curry / raïta de concombre et naans",
                "Poulet au curry tomate crème, raïta de concombre et naans au fromage frais.",
                4,
                25,
                30,
                "Plats principaux",
                IMAGE_PLACEHOLDER,
                Set.of("poulet", "curry", "naans"),
                List.of(
                        ingredientData(ingredientRepository, "Poulet", "Viandes et poissons", "g", "600"),
                        ingredientData(ingredientRepository, "Oignons", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Pâte de curry", "Epicerie salée", "g", "80"),
                        ingredientData(ingredientRepository, "Tomates en dés", "Epicerie salée", "boîte", "1"),
                        ingredientData(ingredientRepository, "Crème fraîche", "Frais", "g", "200"),
                        ingredientData(ingredientRepository, "Concombre", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Yaourt grec", "Frais", "g", "200"),
                        ingredientData(ingredientRepository, "Pâte à pizza", "Frais", "pièce", "1"),
                        ingredientData(ingredientRepository, "Philadelphia", "Frais", "g", "150")
                ),
                "Faire revenir les oignons dans l'huile.",
                "Ajouter la pâte de curry.",
                "Couper le poulet en morceaux, l'ajouter, saler et poivrer.",
                "Ajouter une boîte de tomates en dés.",
                "En fin de cuisson, ajouter la crème fraîche et laisser réduire à découvert.",
                "Préparer le raïta avec oignon très fin, concombre râpé égoutté, yaourt grec, cumin et paprika.",
                "Préparer les naans avec pâte à pizza et Philadelphia au centre, refermer, étaler puis cuire.");

        saveRecipeIfMissing(recipeRepository,
                "Crevettes curry / lait de coco et riz cantonais",
                "Crevettes au curry et lait de coco avec riz cantonais.",
                4,
                10,
                20,
                "Plats principaux",
                IMAGE_PLACEHOLDER,
                Set.of("crevettes", "curry", "riz"),
                List.of(
                        ingredientData(ingredientRepository, "Riz cantonais", "Surgelés", "sachet", "1"),
                        ingredientData(ingredientRepository, "Crevettes", "Viandes et poissons", "g", "500"),
                        ingredientData(ingredientRepository, "Pâte de curry", "Epicerie salée", "g", "60"),
                        ingredientData(ingredientRepository, "Lait de coco", "Epicerie salée", "ml", "400"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Coriandre", "Fruits et légumes", "botte", "1")
                ),
                "Faire cuire le riz cantonais dans une casserole avec un peu d'huile.",
                "Faire cuire les crevettes à la poêle avec huile, sel et poivre.",
                "Ajouter la pâte de curry puis le lait de coco.",
                "Ajouter un demi-citron en fin de cuisson.",
                "Terminer avec de la coriandre.");

        saveRecipeIfMissing(recipeRepository,
                "Brochettes de poulet au miel et à la moutarde",
                "Brochettes de poulet marinées façon pita, miel et moutarde.",
                4,
                15,
                15,
                "Grillades",
                IMAGE_PLACEHOLDER,
                Set.of("poulet", "miel", "moutarde"),
                List.of(
                        ingredientData(ingredientRepository, "Poulet", "Viandes et poissons", "g", "600"),
                        ingredientData(ingredientRepository, "Miel", "Epicerie sucrée", "cuillère", "2"),
                        ingredientData(ingredientRepository, "Moutarde", "Epicerie salée", "cuillère", "2"),
                        ingredientData(ingredientRepository, "Citron", "Fruits et légumes", "pièce", "1"),
                        ingredientData(ingredientRepository, "Paprika", "Epices", "cuillère", "1")
                ),
                "Couper le poulet en morceaux.",
                "Le faire mariner avec huile d'olive, citron, sel, poivre, paprika, miel et moutarde.",
                "Former les brochettes.",
                "Cuire à la poêle, au four ou au barbecue jusqu'à cuisson complète.");

        saveRecipeIfMissing(recipeRepository,
                "Riz sauté à la méditerranéenne",
                "Riz sauté avec poulet ou crevettes, courgettes, tomates, olives et herbes.",
                4,
                10,
                15,
                "Plats rapides",
                IMAGE_PLACEHOLDER,
                Set.of("riz", "méditerranéen", "anti-gaspi"),
                List.of(
                        ingredientData(ingredientRepository, "Riz", "Epicerie salée", "g", "300"),
                        ingredientData(ingredientRepository, "Poulet ou crevettes", "Viandes et poissons", "g", "300"),
                        ingredientData(ingredientRepository, "Courgettes", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Tomates", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Olives", "Epicerie salée", "g", "80"),
                        ingredientData(ingredientRepository, "Herbes", "Fruits et légumes", "botte", "1")
                ));

        saveRecipeIfMissing(recipeRepository,
                "Croque monsieur chèvre frais épinard",
                "Croque monsieur au chèvre fouetté, pousses d'épinard, oignons et noix.",
                4,
                15,
                12,
                "Sandwichs",
                IMAGE_PLACEHOLDER,
                Set.of("croque", "chèvre", "épinards"),
                List.of(
                        ingredientData(ingredientRepository, "Chèvre fouetté nature", "Frais", "pot", "2"),
                        ingredientData(ingredientRepository, "Pain de mie complet", "Boulangerie", "tranche", "8"),
                        ingredientData(ingredientRepository, "Oignons", "Fruits et légumes", "pièce", "2"),
                        ingredientData(ingredientRepository, "Noix", "Epicerie salée", "cerneau", "8"),
                        ingredientData(ingredientRepository, "Pousses d'épinard", "Fruits et légumes", "g", "300")
                ));

        if (carbonara != null || curry != null || salad != null) {
            // Les trois recettes historiques sont volontairement conservées pour les bases fraîches.
        }
    }

    private void seedDishIdeas(RecipeRepository recipeRepository) {
        List<String> names = List.of(
                "Soupe au potiron / carottes / oignon / curry / lardons",
                "Soupe brocolis / Boursin",
                "Soupe à la tomate / crème et riz",
                "Millefeuille de légumes / poulet mariné huile olive vinaigre balsamique",
                "Jardinière de légumes / poulet à la moutarde",
                "Quiche Lorraine",
                "Quiche courgettes / chèvre",
                "Quiche poireaux / lardons",
                "Quiche au saumon / épinards",
                "Quiche au saumon / oseille",
                "Tourte au saumon / Boursin Cuisine",
                "Tourte viande hachée / brocoli / chou-fleur / carottes / curry et lait de coco",
                "Tarte rustique de tomates / Burrata",
                "Tarte rustique de courgettes / Burrata",
                "Tarte à la tomate",
                "Spaghettis bolognaise",
                "Lasagnes au boeuf",
                "Lasagnes au saumon / brocolis ou épinards",
                "Pâtes au saumon / citron / crème et tomates cerise",
                "Curry de cabillaud / poivrons rouges et jaunes / riz basmati",
                "Cabillaud en croûte de crumble parmesan / chorizo / beurre et riz à la tomate au four",
                "Coeur de saumon / spaghettis de courgettes / sauce échalotes citron crème avec riz",
                "Lotte à l'Américaine",
                "Cabillaud sauce vierge de Cyril Lignac",
                "Gratin de ravioles de Romans",
                "Ravioles de Romans à la Toscane",
                "Friands au fromage / salade",
                "Croque-Monsieur jambon / fromage / salade",
                "Croque-Monsieur jambon de pays / avocats / tomates / fromage",
                "Escalopes de poulet panées / Penne aux tomates cerise",
                "Poulet basquaise de Cyril Lignac / riz à la tomate",
                "Boulettes sauce tomates / Burrata sur penne de Cyril Lignac",
                "Tajine de poulet au citron de Cyril Lignac",
                "Cuisses de poulet au citron / pommes de terre au four",
                "Poulet à la mexicaine / poivrons oignons / cheddar / guacamole et tacos",
                "Fajitas de poulet / tomates / cheddar",
                "Fajitas de boeuf haché / tomates / cheddar et galettes mexicaines",
                "Blanquette de poulet / sauce blanche et riz",
                "Boeuf bourguignon / pommes de terre",
                "Boeuf Strogonoff / wedges au four maison",
                "Goulasch à la hongroise / krenleys",
                "Tournedos sauce au poivre ou béarnaise / pommes dauphine",
                "Bavettes à l'échalote / haricots verts / pommes de terre rissolées",
                "Porc au caramel / brocolis et riz",
                "Poulet à l'ananas / brocolis et riz",
                "Poulet Teriyaki / riz basmati",
                "Magret de canard / aubergines à la Parmesane / polenta",
                "Magrets de canard / gratin de potiron / ravioles aux cèpes",
                "Poulet farci jambon de Parme / Mozzarella / bouillon au thym / polenta crémeuse aux tomates cerise",
                "Cordons bleus / Aubergines à la Parmesane / polenta dorée",
                "Osso-Bucco avec pâtes",
                "Bricks au thon / pommes de terre",
                "Samosas aux aubergines",
                "Taboulé vert de Cyril Lignac",
                "Lasagnes de potiron aux cèpes",
                "Conchiglione aux blettes et ricotta",
                "Cannellonis aux épinards",
                "Tarte spirale pommes de terre / jambon et raclette",
                "Crousti-Tartiflette avec reblochon",
                "Crevettes curry",
                "Fajitas",
                "Cannelloni aux blettes et ricotta",
                "Burger",
                "Salade grecque à adapter",
                "Fajitas et guacamole / chips guacamole",
                "Brochettes de poulet au miel / riz safrané",
                "Gnocchis aux petits pois et menthe",
                "Salade de pâtes tomates confites / roquette / jambon de parme / copeaux de parmesan",
                "Cabillaud pané et frites",
                "Salade de pâtes melon / jambon de pays / poivron rouge / tomates",
                "Burgers et frites / saumon à l'ananas"
        );

        for (String name : names) {
            saveDishIdeaIfMissing(recipeRepository, name);
        }
    }

    private void seedSampleMealPlan(RecipeRepository recipeRepository, MealPlanRepository mealPlanRepository) {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (mealPlanRepository.findByWeekStartDate(monday).isPresent()) {
            return;
        }

        Recipe carbonara = recipeRepository.findByNameIgnoreCase("Pates carbonara").orElse(null);
        Recipe salad = recipeRepository.findByNameIgnoreCase("Salade pois chiches tomate").orElse(null);
        Recipe curry = recipeRepository.findByNameIgnoreCase("Poulet curry coco").orElse(null);
        if (carbonara == null || salad == null || curry == null) {
            return;
        }

        MealPlan mealPlan = new MealPlan();
        mealPlan.setWeekStartDate(monday);
        mealPlan.addItem(item(mealPlan, carbonara, monday, MealType.DINNER));
        mealPlan.addItem(item(mealPlan, salad, monday.plusDays(1), MealType.LUNCH));
        mealPlan.addItem(item(mealPlan, curry, monday.plusDays(2), MealType.DINNER));
        mealPlanRepository.save(mealPlan);
    }

    private Recipe saveDishIdeaIfMissing(RecipeRepository recipeRepository, String name) {
        return saveRecipeIfMissing(recipeRepository,
                name,
                "Plat à compléter : ingrédients et étapes à ajouter.",
                4,
                0,
                0,
                "À compléter",
                IMAGE_PLACEHOLDER,
                Set.of("à compléter"),
                List.of());
    }

    private Recipe saveRecipeIfMissing(
            RecipeRepository recipeRepository,
            String name,
            String description,
            int servings,
            int preparationTime,
            int cookingTime,
            String category,
            String imageUrl,
            Set<String> tags,
            List<RecipeIngredientData> ingredientData,
            String... steps
    ) {
        if (recipeRepository.existsByNameIgnoreCase(name)) {
            return null;
        }

        Recipe recipe = recipe(name, description, servings, preparationTime, cookingTime, category, imageUrl, tags, ingredientData);
        addSteps(recipe, steps);
        return recipeRepository.save(recipe);
    }

    private Ingredient ingredient(IngredientRepository repository, String name, String category, String defaultUnit) {
        return repository.findByNameIgnoreCase(name)
                .orElseGet(() -> repository.save(newIngredient(name, category, defaultUnit)));
    }

    private Ingredient newIngredient(String name, String category, String defaultUnit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setShoppingCategory(category);
        ingredient.setDefaultUnit(defaultUnit);
        return ingredient;
    }

    private RecipeIngredientData ingredientData(
            IngredientRepository repository,
            String name,
            String category,
            String unit,
            String quantity
    ) {
        return new RecipeIngredientData(ingredient(repository, name, category, unit), quantity, unit);
    }

    private Recipe recipe(
            String name,
            String description,
            int servings,
            int preparationTime,
            int cookingTime,
            String category,
            String imageUrl,
            Set<String> tags,
            List<RecipeIngredientData> ingredientData
    ) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setServings(servings);
        recipe.setPreparationTimeMinutes(preparationTime);
        recipe.setCookingTimeMinutes(cookingTime);
        recipe.setCategory(category);
        recipe.setImageUrl(imageUrl);
        recipe.setTags(tags);
        for (RecipeIngredientData data : ingredientData) {
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setIngredient(data.ingredient());
            recipeIngredient.setQuantity(new BigDecimal(data.quantity()));
            recipeIngredient.setUnit(data.unit());
            recipe.addIngredient(recipeIngredient);
        }
        return recipe;
    }

    private void addSteps(Recipe recipe, String... descriptions) {
        for (int index = 0; index < descriptions.length; index++) {
            RecipeStep step = new RecipeStep();
            step.setStepOrder(index + 1);
            step.setDescription(descriptions[index]);
            recipe.addStep(step);
        }
    }

    private MealPlanItem item(MealPlan mealPlan, Recipe recipe, LocalDate date, MealType mealType) {
        MealPlanItem item = new MealPlanItem();
        item.setMealPlan(mealPlan);
        item.setRecipe(recipe);
        item.setMealDate(date);
        item.setMealType(mealType);
        return item;
    }

    private record RecipeIngredientData(Ingredient ingredient, String quantity, String unit) {
    }
}
