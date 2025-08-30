package com.gourmich.service;

import com.gourmich.dto.FavoriteDTO;
import com.gourmich.models.Favorite;
import com.gourmich.models.Recipe;
import com.gourmich.models.Users;
import com.gourmich.repo.FavoriteRepository;
import com.gourmich.repo.RecipeRepository;
import com.gourmich.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Transactional
    public List<FavoriteDTO> getFavoritesForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalArgumentException("Utilisateur non authentifié");
        }

        String username = auth.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        List<Favorite> favorites = favoriteRepository.findFavoritesWithRecipeAndAuthor(user.getId());

        return favorites.stream()
                .map(fav -> {
                    Recipe r = fav.getRecipe();
                    return new FavoriteDTO(
                            fav.getId(),
                            r.getId(),
                            r.getTitle(),
                            r.getDescription(),
                            r.getImageUrl(),
                            r.getAuthor().getUsername(),
                            r.getCookingTime(),
                            r.getCategory()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteDTO toggleFavoriteForCurrentUser(Long recipeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalArgumentException("Utilisateur non authentifié");
        }

        String username = auth.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndRecipeId(user.getId(), recipeId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return null;
        } else {
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new RuntimeException("Recette non trouvée"));

            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setRecipe(recipe);

            Favorite saved = favoriteRepository.save(favorite);

            return new FavoriteDTO(
                    saved.getId(),
                    recipe.getId(),
                    recipe.getTitle(),
                    recipe.getDescription(),
                    recipe.getImageUrl(),
                    recipe.getAuthor().getUsername(),
                    recipe.getCookingTime(),
                    recipe.getCategory()
            );
        }
    }

    @Transactional
    public boolean isAlreadyFavoriteForCurrentUser(Long recipeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalArgumentException("Utilisateur non authentifié");
        }

        String username = auth.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        return favoriteRepository.findByUserIdAndRecipeId(user.getId(), recipeId).isPresent();
    }

    @Transactional
    public void removeFavorite(Long userId, Long recipeId) {
        favoriteRepository.deleteByUserIdAndRecipeId(userId, recipeId);
    }

}