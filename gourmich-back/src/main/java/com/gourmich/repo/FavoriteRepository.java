package com.gourmich.repo;

import com.gourmich.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Récupère un favori spécifique
    Optional<Favorite> findByUserIdAndRecipeId(Long userId, Long recipeId);

    // Supprimer un favori
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    // Récupère tous les favoris d'un utilisateur en joignant la recette et son auteur
    @Query("SELECT f FROM Favorite f " +
            "JOIN FETCH f.recipe r " +
            "JOIN FETCH r.author " +
            "WHERE f.user.id = :userId")
    List<Favorite> findFavoritesWithRecipeAndAuthor(@Param("userId") Long userId);

    List<Favorite> findByUserId(Long userId);
}