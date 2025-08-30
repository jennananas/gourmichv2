package com.gourmich.service;

import com.gourmich.dto.IngredientDTO;
import com.gourmich.dto.RecipeDTO;
import com.gourmich.dto.UpdateRecipeDTO;
import com.gourmich.exception.ResourceNotFoundException;
import com.gourmich.exception.UnauthorizedException;
import com.gourmich.models.Ingredient;
import com.gourmich.models.Recipe;
import com.gourmich.models.RecipeCategory;
import com.gourmich.models.Users;
import com.gourmich.repo.IngredientRepository;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeService recipeService;

    private AutoCloseable mocks;
    private Users user;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);

        user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        TestingAuthenticationToken auth = new TestingAuthenticationToken("testuser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        SecurityContextHolder.clearContext();
    }

    // ------------------- createRecipe -------------------
    @Test
    void createRecipe_ShouldSetAuthorAndSave() {
        Recipe recipe = new Recipe();
        recipe.setTitle("My Recipe");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Salt", 1.0, "g", recipe));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(recipeRepository.existsByTitleAndAuthorId("My Recipe", 1L)).thenReturn(false);
        when(recipeRepository.save(recipe)).thenReturn(recipe);

        Recipe saved = recipeService.createRecipe(recipe);

        assertEquals(user, saved.getAuthor());
        assertEquals("My Recipe", saved.getTitle());
        assertEquals(1, saved.getIngredients().size());
        assertEquals("Salt", saved.getIngredients().get(0).getName());
    }

    @Test
    void createRecipe_NoIngredients_ShouldThrow() {
        Recipe recipe = new Recipe();
        recipe.setTitle("My Recipe");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setIngredients(new ArrayList<>()); // vide

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(recipe));
        assertEquals("Ingredients are missing.", ex.getMessage());
    }

    @Test
    void createRecipe_UserNotFound_ShouldThrow() {
        Recipe recipe = new Recipe();
        recipe.setTitle("My Recipe");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setIngredients(List.of(new Ingredient("Salt",1.0,"g",recipe)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(recipe));
        assertEquals("Authenticated user not found", ex.getMessage());
    }

    @Test
    void createRecipe_DuplicateTitle_ShouldThrow() {
        Recipe recipe = new Recipe();
        recipe.setTitle("My Recipe");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setIngredients(List.of(new Ingredient("Salt",1.0,"g",recipe)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(recipeRepository.existsByTitleAndAuthorId("My Recipe", 1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(recipe));
        assertTrue(ex.getMessage().contains("already created a recipe"));
    }

    @Test
    void createRecipe_IngredientsNull_ShouldThrow() {
        Recipe recipe = new Recipe();
        recipe.setTitle("My Recipe");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);
        recipe.setIngredients(null); //

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(recipe));
        assertEquals("Ingredients are missing.", ex.getMessage());
    }

    // ------------------- updateRecipe -------------------
    @Test
    void updateRecipe_Authorized_ShouldUpdate() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Old Title");
        recipe.setAuthor(user);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Pepper", 5.0, "g", recipe));
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);

        RecipeDTO updatedDto = new RecipeDTO();
        updatedDto.setTitle("New Title");
        updatedDto.setDescription("Updated desc");
        updatedDto.setIngredients(new ArrayList<>());
        updatedDto.getIngredients().add(new IngredientDTO("Salt", 10, "g"));
        updatedDto.setCategory(RecipeCategory.MAIN_COURSE);
        updatedDto.setDifficulty(2);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(i -> i.getArguments()[0]);

        UpdateRecipeDTO result = recipeService.updateRecipe(1L, updatedDto);

        assertEquals("New Title", result.getTitle());
        assertEquals(1, result.getIngredients().size());
        assertEquals("Salt", result.getIngredients().get(0).getName());
    }

    @Test
    void updateRecipe_Unauthorized_ShouldThrow() {
        Users otherUser = new Users();
        otherUser.setUsername("other");

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setAuthor(otherUser);
        recipe.setIngredients(new ArrayList<>());
        recipe.getIngredients().add(new Ingredient("Pepper", 5.0, "g", recipe));
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(1);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> recipeService.updateRecipe(1L, new RecipeDTO()));
        assertTrue(ex.getMessage().contains("You're not allowed"));
    }

    @Test
    void updateRecipe_RecipeNotFound_ShouldThrow() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> recipeService.updateRecipe(1L, new RecipeDTO()));
        assertTrue(ex.getMessage().contains("Recipe not found"));
    }

    // ------------------- deleteRecipe -------------------
    @Test
    void deleteRecipe_Authorized_ShouldDelete() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setAuthor(user);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(1L);

        verify(recipeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRecipe_Unauthorized_ShouldThrow() {
        Users otherUser = new Users();
        otherUser.setUsername("other");

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setAuthor(otherUser);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> recipeService.deleteRecipe(1L));
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    void deleteRecipe_RecipeNotFound_ShouldThrow() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> recipeService.deleteRecipe(1L));
        assertTrue(ex.getMessage().contains("Recipe not found"));
    }

    // ------------------- getRecipes -------------------
    @Test
    void getAllRecipes_ShouldReturnListOfRecipes() {
        Recipe r1 = new Recipe();
        Recipe r2 = new Recipe();
        List<Recipe> list = List.of(r1, r2);

        when(recipeRepository.findAll()).thenReturn(list);

        List<Recipe> result = recipeService.getAllRecipes();

        assertEquals(2, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r2));
    }

    @Test
    void getAllRecipes_EmptyList_ShouldReturnEmptyList() {
        when(recipeRepository.findAll()).thenReturn(List.of());

        List<Recipe> result = recipeService.getAllRecipes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRecipeById_ExistingRecipe_ShouldReturnOptionalRecipe() {
        Recipe r = new Recipe();
        r.setId(1L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(r));

        Optional<Recipe> result = recipeService.getRecipeById(1L);

        assertTrue(result.isPresent());
        assertEquals(r, result.get());
    }

    @Test
    void getRecipeById_NonExistingRecipe_ShouldReturnEmptyOptional() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Recipe> result = recipeService.getRecipeById(999L);

        assertTrue(result.isEmpty());
    }

    // ------------------- getLatestRecipes -------------------
    @Test
    void getLatestRecipes_ShouldReturnList() {
        Recipe r1 = new Recipe();
        Recipe r2 = new Recipe();
        List<Recipe> list = List.of(r1, r2);

        when(recipeRepository.findLatestWithRelations(any(Pageable.class))).thenReturn(list);

        List<Recipe> result = recipeService.getLatestRecipes(2);

        assertEquals(2, result.size());
    }

    @Test
    void getLatestRecipes_NGreaterThanListSize_ShouldReturnAll() {
        Recipe r1 = new Recipe();
        List<Recipe> list = List.of(r1);

        when(recipeRepository.findLatestWithRelations(any(Pageable.class))).thenReturn(list);

        List<Recipe> result = recipeService.getLatestRecipes(10);
        assertEquals(1, result.size());
    }

    // ------------------- DTOs -------------------
    @Test
    void toDto_ShouldMapAllFieldsCorrectly() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Pasta");
        recipe.setDescription("Delicious pasta");
        recipe.setImageUrl("pasta.png");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(2);
        recipe.setCookingTime(30L);
        recipe.setInstructions("Cook well");

        Users author = new Users();
        author.setUsername("john");
        recipe.setAuthor(author);

        Ingredient ing1 = new Ingredient("Salt", 5.0, "g", recipe);
        Ingredient ing2 = new Ingredient("Pepper", 2.0, "g", recipe);
        recipe.setIngredients(List.of(ing1, ing2));

        RecipeDTO dto = recipeService.toDto(recipe);

        assertEquals(recipe.getId(), dto.getId());
        assertEquals("Pasta", dto.getTitle());
        assertEquals(2, dto.getIngredients().size());
        assertEquals("Salt", dto.getIngredients().get(0).getName());
        assertEquals("john", dto.getAuthorUsername());
    }

    @Test
    void toDto_NoIngredients_ShouldReturnEmptyIngredientList() {
        Recipe recipe = new Recipe();
        recipe.setId(2L);
        recipe.setTitle("Soup");

        Users author = new Users();
        author.setUsername("alice");
        recipe.setAuthor(author);

        recipe.setIngredients(List.of());

        RecipeDTO dto = recipeService.toDto(recipe);

        assertNotNull(dto.getIngredients());
        assertTrue(dto.getIngredients().isEmpty());
        assertEquals("alice", dto.getAuthorUsername());
    }
}