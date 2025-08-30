package com.gourmich.service;

import com.gourmich.dto.FavoriteDTO;
import com.gourmich.models.Favorite;
import com.gourmich.models.Recipe;
import com.gourmich.models.RecipeCategory;
import com.gourmich.models.Users;
import com.gourmich.repo.FavoriteRepository;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Simule un utilisateur connecté pour tous les tests
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("testuser", null, List.of());
        auth.setAuthenticated(true); // obligatoire pour que le service considère l'utilisateur authentifié
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    // ------------------- getFavoritesForCurrentUser -------------------
    @Test
    void getFavoritesForCurrentUser_NoAuthentication_ShouldThrow() {
        SecurityContextHolder.clearContext();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.getFavoritesForCurrentUser());
        assertEquals("Utilisateur non authentifié", ex.getMessage());
    }

    @Test
    void getFavoritesForCurrentUser_UserNotFound_ShouldThrow() {
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("ghostuser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.getFavoritesForCurrentUser());
        assertEquals("Utilisateur non trouvé", ex.getMessage());
    }
    @Test
    void getFavoritesForCurrentUser_ShouldReturnList() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Recette 1");
        recipe.setAuthor(user);
        recipe.setCategory(RecipeCategory.MAIN_COURSE);

        Favorite fav = new Favorite();
        fav.setId(1L);
        fav.setRecipe(recipe);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findFavoritesWithRecipeAndAuthor(1L)).thenReturn(List.of(fav));

        List<FavoriteDTO> result = favoriteService.getFavoritesForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getRecipeId());
        assertEquals("Recette 1", result.get(0).getTitle());
    }

    @Test
    void getFavoritesForCurrentUser_NoAuth_ShouldThrow() {
        SecurityContextHolder.clearContext();
        assertThrows(IllegalArgumentException.class, () -> favoriteService.getFavoritesForCurrentUser());
    }

    // ------------------- toggleFavoriteForCurrentUser -------------------
    @Test
    void toggleFavoriteForCurrentUser_AddFavorite() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Recette 1");
        recipe.setAuthor(user);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserIdAndRecipeId(1L, 10L)).thenReturn(Optional.empty());
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(i -> i.getArguments()[0]);

        FavoriteDTO result = favoriteService.toggleFavoriteForCurrentUser(10L);
        assertNotNull(result);
        assertEquals(10L, result.getRecipeId());
    }

    @Test
    void toggleFavoriteForCurrentUser_RemoveFavorite() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Recette 1");

        Favorite fav = new Favorite();
        fav.setId(100L);
        fav.setRecipe(recipe);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserIdAndRecipeId(1L, 10L)).thenReturn(Optional.of(fav));

        FavoriteDTO result = favoriteService.toggleFavoriteForCurrentUser(10L);
        assertNull(result);
        verify(favoriteRepository, times(1)).delete(fav);
    }

    @Test
    void toggleFavoriteForCurrentUser_NoAuthentication_ShouldThrow() {
        SecurityContextHolder.clearContext();
        assertThrows(IllegalArgumentException.class, () ->
                        favoriteService.toggleFavoriteForCurrentUser(10L),
                "Utilisateur non authentifié"
        );
    }

    @Test
    void toggleFavoriteForCurrentUser_UserNotFound_ShouldThrow() {
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("ghostuser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                        favoriteService.toggleFavoriteForCurrentUser(10L),
                "Utilisateur non trouvé"
        );
    }

    @Test
    void toggleFavoriteForCurrentUser_RecipeNotFound_ShouldThrow() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("testuser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserIdAndRecipeId(1L, 10L)).thenReturn(Optional.empty());
        when(recipeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                        favoriteService.toggleFavoriteForCurrentUser(10L),
                "Recette non trouvée"
        );
    }

    // ------------------- isAlreadyFavoriteForCurrentUser -------------------
    @Test
    void isAlreadyFavoriteForCurrentUser_ReturnsTrue() {
        Users user = new Users();
        user.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserIdAndRecipeId(1L, 10L)).thenReturn(Optional.of(new Favorite()));

        assertTrue(favoriteService.isAlreadyFavoriteForCurrentUser(10L));
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_ReturnsFalse() {
        Users user = new Users();
        user.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserIdAndRecipeId(1L, 10L)).thenReturn(Optional.empty());

        assertFalse(favoriteService.isAlreadyFavoriteForCurrentUser(10L));
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_NoAuthentication_ShouldThrow() {
        SecurityContextHolder.clearContext();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.isAlreadyFavoriteForCurrentUser(10L));

        assertEquals("Utilisateur non authentifié", ex.getMessage());
    }

    // ------------------- removeFavorite -------------------
    @Test
    void removeFavorite_ShouldCallRepositoryDelete() {
        favoriteService.removeFavorite(1L, 10L);
        verify(favoriteRepository, times(1)).deleteByUserIdAndRecipeId(1L, 10L);
    }
}