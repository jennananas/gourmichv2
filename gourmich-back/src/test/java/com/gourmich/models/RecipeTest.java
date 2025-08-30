package com.gourmich.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void gettersAndSetters_ShouldWork() {
        Recipe recipe = new Recipe();
        Users author = new Users("test@example.com", "user", "pass");

        recipe.setTitle("Pasta");
        recipe.setDescription("Delicious");
        recipe.setImageUrl("image.png");
        recipe.setCategory(RecipeCategory.MAIN_COURSE);
        recipe.setDifficulty(2);
        recipe.setCookingTime(30L);
        recipe.setInstructions("Boil pasta");
        recipe.setAuthor(author);

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("Salt", 1.0, "tsp", recipe));
        recipe.setIngredients(ingredients);

        assertEquals("Pasta", recipe.getTitle());
        assertEquals("Delicious", recipe.getDescription());
        assertEquals("image.png", recipe.getImageUrl());
        assertEquals(RecipeCategory.MAIN_COURSE, recipe.getCategory());
        assertEquals(2, recipe.getDifficulty());
        assertEquals(30L, recipe.getCookingTime());
        assertEquals("Boil pasta", recipe.getInstructions());
        assertEquals(author, recipe.getAuthor());
        assertEquals(ingredients, recipe.getIngredients());
    }

    @Test
    void onCreate_ShouldSetCreatedAt() {
        Recipe recipe = new Recipe();
        LocalDateTime before = LocalDateTime.now();

        recipe.onCreate();
        LocalDateTime after = recipe.getCreatedAt();

        assertNotNull(after, "createdAt doit être défini");
        assertFalse(after.isBefore(before), "createdAt doit être égal ou après la date avant onCreate()");
    }
}