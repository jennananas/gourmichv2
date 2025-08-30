package com.gourmich.controller;

import com.gourmich.dto.IngredientDTO;
import com.gourmich.dto.RecipeDTO;
import com.gourmich.dto.UpdateRecipeDTO;
import com.gourmich.exception.ResourceNotFoundException;
import com.gourmich.models.Recipe;
import com.gourmich.models.UserPrincipal;
import com.gourmich.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;


    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody Recipe recipe) {
        try {
            Recipe newRecipe = recipeService.createRecipe(recipe);
            return ResponseEntity.ok(newRecipe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne lors de la création de la recette");
        }
    }

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> dtos = recipeService.getAllRecipes().stream()
                .map(recipeService::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<RecipeDTO>> getLatestRecipes(@RequestParam(defaultValue = "3") int n) {
        List<Recipe> latestRecipes = recipeService.getLatestRecipes(n);

        List<RecipeDTO> dtoList = latestRecipes.stream()
                .map(recipe -> recipeService.toDto(recipe))
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
                .map(recipeService::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/by-id/{id}")
    public ResponseEntity<UpdateRecipeDTO> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO updatedRecipeDto) {

        UpdateRecipeDTO updated = recipeService.updateRecipe(id, updatedRecipeDto);
        return ResponseEntity.ok(updated);
    }
}