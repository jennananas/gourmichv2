package com.gourmich.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void constructorAndGetters_ShouldReturnCorrectValues() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Pasta");

        Ingredient ingredient = new Ingredient("Salt", 10.0, "g", recipe);

        assertEquals("Salt", ingredient.getName());
        assertEquals(10.0, ingredient.getQuantity());
        assertEquals("g", ingredient.getUnit());
        assertEquals(recipe, ingredient.getRecipe());
    }

    @Test
    void setters_ShouldUpdateValues() {
        Ingredient ingredient = new Ingredient();
        Recipe recipe = new Recipe();
        recipe.setId(1L);

        ingredient.setName("Pepper");
        ingredient.setQuantity(5.5);
        ingredient.setUnit("g");
        ingredient.setRecipe(recipe);

        assertEquals("Pepper", ingredient.getName());
        assertEquals(5.5, ingredient.getQuantity());
        assertEquals("g", ingredient.getUnit());
        assertEquals(recipe, ingredient.getRecipe());
    }
}