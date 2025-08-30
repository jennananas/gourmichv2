package com.gourmich.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeCategoryTest {

    @Test
    void fromValue_ShouldReturnCorrectEnum_WhenLabelMatches() {
        assertEquals(RecipeCategory.MAIN_COURSE, RecipeCategory.fromValue("Main Course"));
        assertEquals(RecipeCategory.DESSERT, RecipeCategory.fromValue("Dessert"));
        assertEquals(RecipeCategory.SNACK, RecipeCategory.fromValue("Snack"));
    }

    @Test
    void fromValue_ShouldReturnCorrectEnum_WhenNameMatches() {
        assertEquals(RecipeCategory.MAIN_COURSE, RecipeCategory.fromValue("MAIN_COURSE"));
        assertEquals(RecipeCategory.DRINK, RecipeCategory.fromValue("DRINK"));
        assertEquals(RecipeCategory.STARTER, RecipeCategory.fromValue("STARTER"));
    }

    @Test
    void fromValue_ShouldTrimInput() {
        assertEquals(RecipeCategory.SIDE_DISH, RecipeCategory.fromValue("  Side Dish  "));
    }

    @Test
    void fromValue_ShouldThrow_WhenValueIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RecipeCategory.fromValue(null));
        assertEquals("Category value is null", ex.getMessage());
    }

    @Test
    void fromValue_ShouldThrow_WhenValueUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RecipeCategory.fromValue("UnknownCategory"));
        assertTrue(ex.getMessage().contains("Unknown category"));
    }

    @Test
    void getLabel_ShouldReturnLabel() {
        assertEquals("Main Course", RecipeCategory.MAIN_COURSE.getLabel());
        assertEquals("Dessert", RecipeCategory.DESSERT.getLabel());
    }
}