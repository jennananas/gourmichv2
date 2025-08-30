package com.gourmich.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteTest {

    @Test
    void constructorAndGetters_ShouldReturnCorrectValues() {
        Users user = new Users("john@example.com", "john", "secret");
        user.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setId(2L);
        recipe.setTitle("Pasta");

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);

        assertEquals(user, favorite.getUser());
        assertEquals(recipe, favorite.getRecipe());
        assertNotNull(favorite.getAddAt());
        assertTrue(favorite.getAddAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void setters_ShouldUpdateValues() {
        Favorite favorite = new Favorite();

        Users user = new Users();
        Recipe recipe = new Recipe();

        favorite.setUser(user);
        favorite.setRecipe(recipe);
        LocalDateTime now = LocalDateTime.now();
        favorite.setAddAt(now);

        assertEquals(user, favorite.getUser());
        assertEquals(recipe, favorite.getRecipe());
        assertEquals(now, favorite.getAddAt());
    }
}