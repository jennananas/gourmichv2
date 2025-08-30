package com.gourmich.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.IngredientDTO;
import com.gourmich.dto.RecipeDTO;
import com.gourmich.dto.UpdateRecipeDTO;
import com.gourmich.exception.ResourceNotFoundException;
import com.gourmich.models.Recipe;
import com.gourmich.models.RecipeCategory;
import com.gourmich.service.JWTService;
import com.gourmich.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------- CREATE -------------------
    @Test
    void createRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");

        Recipe createdRecipe = new Recipe();
        createdRecipe.setId(1L);
        createdRecipe.setTitle("Test Recipe");

        when(recipeService.createRecipe(any(Recipe.class))).thenReturn(createdRecipe);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Recipe"));
    }

    @Test
    void createRecipe_WhenIllegalArgument_ShouldReturnBadRequest() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Invalid Recipe");

        when(recipeService.createRecipe(any(Recipe.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid data"));
    }

    @Test
    void createRecipe_WhenUnexpectedError_ShouldReturnInternalServerError() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test");

        when(recipeService.createRecipe(any(Recipe.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erreur interne lors de la création de la recette"));
    }

    // ------------------- GET -------------------
    @Test
    void testGetAllRecipes() throws Exception {
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setTitle("Recette 1");

        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setTitle("Recette 2");

        RecipeDTO dto1 = new RecipeDTO();
        dto1.setId(1L);
        dto1.setTitle("Recette 1");

        RecipeDTO dto2 = new RecipeDTO();
        dto2.setId(2L);
        dto2.setTitle("Recette 2");

        when(recipeService.getAllRecipes()).thenReturn(List.of(recipe1, recipe2));
        when(recipeService.toDto(recipe1)).thenReturn(dto1);
        when(recipeService.toDto(recipe2)).thenReturn(dto2);

        mockMvc.perform(get("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Recette 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Recette 2"));
    }

    @Test
    void testGetLatestRecipes() throws Exception {
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setTitle("Recette la plus récente 1");

        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setTitle("Recette la plus récente 2");

        RecipeDTO dto1 = new RecipeDTO();
        dto1.setId(1L);
        dto1.setTitle("Recette la plus récente 1");

        RecipeDTO dto2 = new RecipeDTO();
        dto2.setId(2L);
        dto2.setTitle("Recette la plus récente 2");

        when(recipeService.getLatestRecipes(3)).thenReturn(List.of(recipe1, recipe2));
        when(recipeService.toDto(recipe1)).thenReturn(dto1);
        when(recipeService.toDto(recipe2)).thenReturn(dto2);

        mockMvc.perform(get("/api/recipes/latest")
                        .param("n", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Recette la plus récente 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Recette la plus récente 2"));
    }

    @Test
    void testGetRecipeById() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Recette par ID");

        RecipeDTO dto = new RecipeDTO();
        dto.setId(1L);
        dto.setTitle("Recette par ID");

        when(recipeService.getRecipeById(1L)).thenReturn(Optional.of(recipe));
        when(recipeService.toDto(recipe)).thenReturn(dto);

        mockMvc.perform(get("/api/recipes/by-id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Recette par ID"));
    }

    @Test
    void testGetRecipeByIdNotFound() throws Exception {
        when(recipeService.getRecipeById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/by-id/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ------------------- DELETE -------------------

    @Test
    void testDeleteRecipe() throws Exception {
        doNothing().when(recipeService).deleteRecipe(1L);

        mockMvc.perform(delete("/api/recipes/by-id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRecipeNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Recipe not found")).when(recipeService).deleteRecipe(999L);

        mockMvc.perform(delete("/api/recipes/by-id/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // ------------------- UPDATE -------------------
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateRecipe() throws Exception {
        RecipeDTO updatedDto = new RecipeDTO();
        updatedDto.setTitle("Updated Recipe");
        updatedDto.setDescription("Updated description");
        updatedDto.setCategory(RecipeCategory.DESSERT);
        updatedDto.setDifficulty(1);
        updatedDto.setCookingTime(10L);
        updatedDto.setInstructions("Mix and cook");
        updatedDto.setIngredients(List.of(new IngredientDTO("Flour", 100.0, "g")));

        UpdateRecipeDTO updated = new UpdateRecipeDTO();
        updated.setId(1L);
        updated.setTitle("Updated Recipe");
        updated.setDescription("Updated description");

        when(recipeService.updateRecipe(eq(1L), any(RecipeDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/recipes/by-id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Recipe"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }


    @Test
    void updateRecipe_WhenIllegalArgument_ShouldReturnBadRequest() throws Exception {
        RecipeDTO updatedDto = new RecipeDTO();
        updatedDto.setTitle("Updated Recipe");
        updatedDto.setDescription("Updated description");
        updatedDto.setCategory(RecipeCategory.DESSERT);
        updatedDto.setDifficulty(1);
        updatedDto.setCookingTime(10L);
        updatedDto.setInstructions("Mix and cook");
        updatedDto.setIngredients(List.of(new IngredientDTO("Flour", 100.0, "g")));

        when(recipeService.updateRecipe(eq(1L), any(RecipeDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid update"));

        mockMvc.perform(put("/api/recipes/by-id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRecipe_WhenUnexpectedError_ShouldReturnInternalServerError() throws Exception {
        RecipeDTO updatedDto = new RecipeDTO();
        updatedDto.setTitle("Updated Recipe");
        updatedDto.setDescription("Updated description");
        updatedDto.setCategory(RecipeCategory.DESSERT);
        updatedDto.setDifficulty(1);
        updatedDto.setCookingTime(10L);
        updatedDto.setInstructions("Mix and cook");
        updatedDto.setIngredients(List.of(new IngredientDTO("Flour", 100.0, "g")));

        when(recipeService.updateRecipe(eq(1L), any(RecipeDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/recipes/by-id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isInternalServerError());
    }

}