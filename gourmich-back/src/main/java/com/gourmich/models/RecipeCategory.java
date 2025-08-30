package com.gourmich.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum RecipeCategory {
    MAIN_COURSE("Main Course"),
    SIDE_DISH("Side Dish"),
    DESSERT("Dessert"),
    DRINK("Drink"),
    SNACK("Snack"),
    STARTER("Starter");

    private final String label;

    RecipeCategory(String label) {
        this.label = label;
    }

    @JsonCreator
    public static RecipeCategory fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Category value is null");
        }
        String trimmed = value.trim();
        for (RecipeCategory category : values()) {
            if (category.label.equalsIgnoreCase(trimmed) || category.name().equalsIgnoreCase(trimmed)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
