package com.gourmich.service;

import com.gourmich.dto.IngredientDTO;
import com.gourmich.dto.RecipeDTO;
import com.gourmich.dto.UpdateRecipeDTO;
import com.gourmich.models.Ingredient;
import com.gourmich.models.Recipe;
import com.gourmich.models.RecipeCategory;
import com.gourmich.models.Users;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class RecipeServiceIT {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private Users author;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        author = new Users();
        author.setUsername("chef");
        author.setEmail("chef@example.com");
        author.setPassword("secret");
        userRepository.save(author);

        // simuler l'utilisateur connecté
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("chef", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRecipe_ShouldSaveRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Pasta");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(2);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Salt", 1.0, "g", recipe));

        Recipe saved = recipeService.createRecipe(recipe);

        assertNotNull(saved.getId());
        assertEquals("Pasta", saved.getTitle());
        assertEquals(1, saved.getIngredients().size());
        assertEquals("chef", saved.getAuthor().getUsername());
    }

    @Test
    void createRecipe_NoIngredients_ShouldThrow() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Empty Dish");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setIngredients(new ArrayList<>());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(recipe));
        assertEquals("Ingredients are missing.", ex.getMessage());
    }

    @Test
    void updateRecipe_Authorized_ShouldUpdate() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Old Dish");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setAuthor(author);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Pepper", 2.0, "g", recipe));
        recipeRepository.save(recipe);

        RecipeDTO dto = new RecipeDTO();
        dto.setTitle("New Dish");
        dto.setCategory(RecipeCategory.MAIN_COURSE);
        dto.setDifficulty(3);
        dto.setIngredients(new ArrayList<>());
        dto.getIngredients().add(new IngredientDTO("Salt", 5.0, "g"));

        UpdateRecipeDTO updated = recipeService.updateRecipe(recipe.getId(), dto);

        assertEquals("New Dish", updated.getTitle());
        assertEquals(1, updated.getIngredients().size());
        assertEquals("Salt", updated.getIngredients().get(0).getName());
    }

    @Test
    void deleteRecipe_Authorized_ShouldDelete() {
        Recipe recipe = new Recipe();
        recipe.setTitle("ToDelete");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setAuthor(author);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Pepper", 2.0, "g", recipe));
        recipeRepository.save(recipe);

        recipeService.deleteRecipe(recipe.getId());
        Optional<Recipe> found = recipeRepository.findById(recipe.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void getLatestRecipes_ShouldReturnList() {
        Recipe r1 = new Recipe();
        r1.setTitle("Dish1");
        r1.setCategory(RecipeCategory.MAIN_COURSE);
        r1.setDifficulty(1);
        r1.setAuthor(author);
        r1.setIngredients(new ArrayList<>());
        r1.getIngredients().add(new Ingredient("Salt", 1.0, "g", r1));
        recipeRepository.save(r1);

        Recipe r2 = new Recipe();
        r2.setTitle("Dish2");
        r2.setCategory(RecipeCategory.MAIN_COURSE);
        r2.setDifficulty(2);
        r2.setAuthor(author);
        r2.setIngredients(new ArrayList<>());
        r2.getIngredients().add(new Ingredient("Pepper", 2.0, "g", r2));
        recipeRepository.save(r2);

        List<Recipe> latest = recipeService.getLatestRecipes(2);
        assertEquals(2, latest.size());
    }
}