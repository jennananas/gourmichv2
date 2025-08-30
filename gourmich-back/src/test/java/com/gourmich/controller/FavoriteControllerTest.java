package com.gourmich.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.FavoriteDTO;
import com.gourmich.models.RecipeCategory;
import com.gourmich.service.FavoriteService;
import com.gourmich.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @MockitoBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "testuser")
    void testGetFavorites() throws Exception {
        FavoriteDTO dto = new FavoriteDTO(
                1L,
                10L,
                "Test Recipe",
                "Description",
                "http://image.jpg",
                "author",
                30L,
                RecipeCategory.MAIN_COURSE
        );

        when(favoriteService.getFavoritesForCurrentUser())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipeId").value(10L))
                .andExpect(jsonPath("$[0].title").value("Test Recipe"))
                .andExpect(jsonPath("$[0].authorUsername").value("author"));
    }

    @Test
    @WithAnonymousUser
    void testGetFavorites_unauthorized() throws Exception {
        when(favoriteService.getFavoritesForCurrentUser())
                .thenThrow(new IllegalArgumentException("Utilisateur non authentifié"));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Utilisateur non authentifié"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testToggleFavorite_addFavorite() throws Exception {
        FavoriteDTO dto = new FavoriteDTO(
                1L,
                10L,
                "Test Recipe",
                "Description",
                "http://image.jpg",
                "author",
                30L,
                RecipeCategory.MAIN_COURSE
        );

        when(favoriteService.toggleFavoriteForCurrentUser(10L)).thenReturn(dto);

        mockMvc.perform(post("/api/favorites/toggle")
                        .param("recipeId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(10L))
                .andExpect(jsonPath("$.title").value("Test Recipe"))
                .andExpect(jsonPath("$.authorUsername").value("author"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testToggleFavorite_removeFavorite() throws Exception {
        when(favoriteService.toggleFavoriteForCurrentUser(anyLong()))
                .thenReturn(null);

        mockMvc.perform(post("/api/favorites/toggle")
                        .param("recipeId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Unfav recipe"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testToggleFavorite_internalServerError() throws Exception {
        when(favoriteService.toggleFavoriteForCurrentUser(10L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/favorites/toggle")
                        .param("recipeId", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erreur interne lors du toggle du favori"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testIsFavorite_true() throws Exception {
        when(favoriteService.isAlreadyFavoriteForCurrentUser(10L)).thenReturn(true);

        mockMvc.perform(get("/api/favorites/is-favorite/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true")); // la réponse est juste un booléen
    }

    @Test
    @WithMockUser(username = "testuser")
    void testIsFavorite_false() throws Exception {
        when(favoriteService.isAlreadyFavoriteForCurrentUser(10L)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/is-favorite/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithAnonymousUser
    void testIsFavorite_unauthorized() throws Exception {
        when(favoriteService.isAlreadyFavoriteForCurrentUser(10L))
                .thenThrow(new IllegalArgumentException("Utilisateur non authentifié"));

        mockMvc.perform(get("/api/favorites/is-favorite/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Utilisateur non authentifié"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testIsFavorite_internalServerError() throws Exception {
        when(favoriteService.isAlreadyFavoriteForCurrentUser(10L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/favorites/is-favorite/10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erreur interne lors de la vérification du favori"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetFavorites_internalServerError() throws Exception {
        when(favoriteService.getFavoritesForCurrentUser())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erreur interne lors de la récupération des favoris"));
    }

    @Test
    @WithAnonymousUser
    void testToggleFavorite_unauthorized() throws Exception {
        when(favoriteService.toggleFavoriteForCurrentUser(10L))
                .thenThrow(new IllegalArgumentException("Utilisateur non authentifié"));

        mockMvc.perform(post("/api/favorites/toggle")
                        .param("recipeId", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Utilisateur non authentifié"));
    }





}