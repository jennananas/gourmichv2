package com.gourmich.service;
import com.gourmich.dto.IngredientDTO;
import com.gourmich.dto.UpdateRecipeDTO;
import com.gourmich.exception.ResourceNotFoundException;
import com.gourmich.exception.UnauthorizedException;
import com.gourmich.models.Ingredient;
import com.gourmich.repo.IngredientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.gourmich.dto.RecipeDTO;
import com.gourmich.models.Recipe;
import com.gourmich.models.RecipeCategory;
import com.gourmich.models.Users;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Recipe createRecipe(Recipe recipe) throws IllegalArgumentException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        if (recipeRepository.existsByTitleAndAuthorId(recipe.getTitle(), author.getId())) {
            throw new IllegalArgumentException("This user already created a recipe with the title: " + recipe.getTitle());
        }

        recipe.setAuthor(author);

        RecipeCategory category = RecipeCategory.valueOf(recipe.getCategory().toString().toUpperCase());
        recipe.setCategory(category);

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Ingredients are missing.");
        } else {
            recipe.getIngredients().forEach(ingredient -> ingredient.setRecipe(recipe));
        }

        return recipeRepository.save(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public RecipeDTO toDto(Recipe recipe) {
        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(ingredient -> new IngredientDTO(
                        ingredient.getName(),
                        ingredient.getQuantity(),
                        ingredient.getUnit()
                ))
                .collect(Collectors.toList());

        return new RecipeDTO(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getImageUrl(),
                recipe.getCategory(),
                recipe.getDifficulty(),
                recipe.getCookingTime(),
                ingredientDTOs,
                recipe.getInstructions(),
                recipe.getAuthor().getUsername()
        );
    }

    public UpdateRecipeDTO toDTO(Recipe recipe) {
        UpdateRecipeDTO dto = new UpdateRecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setImageUrl(recipe.getImageUrl());
        dto.setCategory(String.valueOf(recipe.getCategory()));
        dto.setDifficulty(recipe.getDifficulty());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setInstructions(recipe.getInstructions());
        dto.setAuthorUsername(recipe.getAuthor().getUsername());

        List<IngredientDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(ing -> {
                    IngredientDTO iDto = new IngredientDTO();
                    iDto.setName(ing.getName());
                    iDto.setQuantity(ing.getQuantity());
                    iDto.setUnit(ing.getUnit());
                    return iDto;
                })
                .collect(Collectors.toList());

        dto.setIngredients(ingredientDTOs);
        return dto;
    }

    public void deleteRecipe(Long id){
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (!recipe.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this recipe.");
        }
        recipeRepository.deleteById(id);
    }

    public UpdateRecipeDTO updateRecipe(Long id, RecipeDTO updatedRecipeDto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id : " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // récupère le username de l'utilisateur connecté

        if (!recipe.getAuthor().getUsername().equals(username)) {
            throw new UnauthorizedException("You're not allowed to edit a recipe that isn't yours.");
        }

        recipe.setTitle(updatedRecipeDto.getTitle());
        recipe.setDescription(updatedRecipeDto.getDescription());
        recipe.setImageUrl(updatedRecipeDto.getImageUrl());
        recipe.setCategory(updatedRecipeDto.getCategory());
        recipe.setDifficulty(updatedRecipeDto.getDifficulty());
        recipe.setCookingTime(updatedRecipeDto.getCookingTime());
        recipe.setInstructions(updatedRecipeDto.getInstructions());

        recipe.getIngredients().clear();
        for (IngredientDTO ingDto : updatedRecipeDto.getIngredients()) {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(ingDto.getName());
            ingredient.setQuantity(ingDto.getQuantity());
            ingredient.setUnit(ingDto.getUnit());
            ingredient.setRecipe(recipe);
            recipe.getIngredients().add(ingredient);
        }

        Recipe saved = recipeRepository.save(recipe);
        return toDTO(saved);
    }

    public List<Recipe> getLatestRecipes(int n) {
        Pageable limit = PageRequest.of(0, n);
        return recipeRepository.findLatestWithRelations(limit);
    }
}
