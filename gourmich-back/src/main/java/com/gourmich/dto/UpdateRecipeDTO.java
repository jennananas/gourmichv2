package com.gourmich.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRecipeDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private int difficulty;
    private Long cookingTime;
    private String instructions;
    private String authorUsername;
    private List<IngredientDTO> ingredients;
}
