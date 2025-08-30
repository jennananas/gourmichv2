package com.gourmich.dto;

import com.gourmich.dto.IngredientDTO;
import com.gourmich.models.RecipeCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecipeDTO {
    private Long id;

    @NotBlank(message = "Title is required.")
    @Size(min = 4, max = 100, message = "Title must be between 4 and 100 characters long.")
    private String title;

    @Pattern(
            regexp = "^[a-zA-Z0-9\\s.,!?()'\"«»;:\\-]*$",
            message = "Description contains invalid characters"
    )
    private String description;

    @Pattern(
            regexp = "^(https?:\\/\\/.*\\.(?:png|jpg|jpeg|gif|webp))$",
            message = "Please provide a valid image URL"
    )
    private String imageUrl;

    @NotNull(message = "Category is required.")
    private RecipeCategory category;

    @NotNull(message = "Difficulty is required.")
    @Min(value = 1, message = "Difficulty must be at least 1.")
    private Integer difficulty;

    @NotNull(message = "Cooking time is required.")
    @Min(value = 1, message = "Cooking time must be at least 1 minute.")
    private Long cookingTime;

    @NotNull(message = "Ingredients are required.")
    @Size(min = 1, message = "At least one ingredient is required.")
    private List<IngredientDTO> ingredients;

    @NotBlank(message = "Steps are required.")
    private String instructions;

    private String authorUsername;
}