package com.gourmich.service;

import com.gourmich.dto.FavoriteDTO;
import com.gourmich.models.Favorite;
import com.gourmich.models.Users;
import com.gourmich.repo.FavoriteRepository;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import com.gourmich.models.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FavoriteServiceIT {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private Users user;
    private Recipe recipe;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        userRepository.save(user);

        recipe = new Recipe();
        recipe.setTitle("Pasta");
        recipe.setDescription("Delicious pasta");
        recipe.setAuthor(user);
        recipeRepository.save(recipe);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), "secret", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getFavoritesForCurrentUser_ShouldReturnFavorites_WhenUserHasFavorites() {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);
        favoriteRepository.save(favorite);

        // Act
        List<FavoriteDTO> favorites = favoriteService.getFavoritesForCurrentUser();

        // Assert
        assertNotNull(favorites);
        assertEquals(1, favorites.size());
        assertEquals("Pasta", favorites.get(0).getTitle());
    }

    @Test
    void getFavoritesForCurrentUser_ShouldReturnEmptyList_WhenUserHasNoFavorites() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john", "secret", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        List<FavoriteDTO> favorites = favoriteService.getFavoritesForCurrentUser();

        assertNotNull(favorites);
        assertTrue(favorites.isEmpty());
    }

    @Test
    void getFavoritesForCurrentUser_ShouldThrowException_WhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalArgumentException.class, () -> favoriteService.getFavoritesForCurrentUser());
    }

    @Test
    void getFavoritesForCurrentUser_ShouldThrowException_WhenUserNotFound() {
        Authentication auth = new UsernamePasswordAuthenticationToken("ghost", "secret", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalArgumentException.class, () -> favoriteService.getFavoritesForCurrentUser());
    }

    @Test
    void toggleFavorite_ShouldAddFavorite_WhenNotExists() {
        FavoriteDTO favoriteDTO = favoriteService.toggleFavoriteForCurrentUser(recipe.getId());

        assertNotNull(favoriteDTO);
        assertEquals(recipe.getTitle(), favoriteDTO.getTitle());
        assertEquals(1, favoriteRepository.count());
    }

    @Test
    void toggleFavorite_ShouldRemoveFavorite_WhenAlreadyExists() {
        FavoriteDTO firstCall = favoriteService.toggleFavoriteForCurrentUser(recipe.getId());
        assertNotNull(firstCall);

        FavoriteDTO result = favoriteService.toggleFavoriteForCurrentUser(recipe.getId());

        assertNull(result);
        assertEquals(0, favoriteRepository.count());
    }

    @Test
    void toggleFavorite_ShouldThrow_WhenUserNotAuthenticated() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> favoriteService.toggleFavoriteForCurrentUser(recipe.getId()));
    }

    @Test
    void toggleFavorite_ShouldThrow_WhenUserNotFoundInDB() {
        // Arrange
        Authentication fakeAuth = new UsernamePasswordAuthenticationToken("ghost", "pw", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(fakeAuth);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> favoriteService.toggleFavoriteForCurrentUser(recipe.getId()));
    }

    @Test
    void toggleFavorite_ShouldThrow_WhenRecipeNotFound() {
        // Act + Assert
        assertThrows(RuntimeException.class,
                () -> favoriteService.toggleFavoriteForCurrentUser(999L)); // id inexistant
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_ShouldReturnTrue_WhenRecipeIsFavorited() {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);
        favoriteRepository.save(favorite);

        boolean result = favoriteService.isAlreadyFavoriteForCurrentUser(recipe.getId());

        assertTrue(result);
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_ShouldReturnFalse_WhenRecipeIsNotFavorited() {
        boolean result = favoriteService.isAlreadyFavoriteForCurrentUser(recipe.getId());

        assertFalse(result);
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_ShouldThrowException_WhenNoUserAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalArgumentException.class, () ->
                favoriteService.isAlreadyFavoriteForCurrentUser(recipe.getId())
        );
    }

    @Test
    void removeFavorite_ShouldDelete_WhenFavoriteExists() {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);
        favoriteRepository.save(favorite);

        assertEquals(1, favoriteRepository.count());

        favoriteService.removeFavorite(user.getId(), recipe.getId());

        assertEquals(0, favoriteRepository.count(), "Le favori doit être supprimé");
    }

    @Test
    void removeFavorite_ShouldDoNothing_WhenFavoriteDoesNotExist() {
        assertEquals(0, favoriteRepository.count());

        favoriteService.removeFavorite(user.getId(), recipe.getId());

        assertEquals(0, favoriteRepository.count(), "Aucun favori ne doit être supprimé");
    }

    @Test
    void removeFavorite_ShouldDoNothing_WhenUserOrRecipeIdInvalid() {
        long invalidUserId = 999L;
        long invalidRecipeId = 888L;

        assertEquals(0, favoriteRepository.count());

        favoriteService.removeFavorite(invalidUserId, invalidRecipeId);

        assertEquals(0, favoriteRepository.count(), "Aucun favori ne doit être supprimé avec des IDs invalides");
    }

    @Test
    void isAlreadyFavoriteForCurrentUser_ShouldThrow_WhenUserNotFoundInDB() {
        Authentication fakeAuth = new UsernamePasswordAuthenticationToken("ghost", "secret", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(fakeAuth);

        assertThrows(IllegalArgumentException.class, () ->
                        favoriteService.isAlreadyFavoriteForCurrentUser(recipe.getId()),
                "Doit lever une exception si l'utilisateur authentifié n'existe pas en DB"
        );
    }
}
