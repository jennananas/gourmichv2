package com.gourmich.dto;

import com.gourmich.models.RecipeCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteDTO {
    private Long id;
    private Long recipeId;
    private String title;
    private String description;
    private String imageUrl;
    private String authorUsername;
    private Long cookingTime;
    private RecipeCategory category;

    public FavoriteDTO(Long id, Long recipeId, String title, String description, String imageUrl,
                       String authorUsername, Long cookingTime, RecipeCategory category) {
        this.id = id;
        this.recipeId = recipeId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.authorUsername = authorUsername;
        this.cookingTime = cookingTime;
        this.category = category;
    }
}