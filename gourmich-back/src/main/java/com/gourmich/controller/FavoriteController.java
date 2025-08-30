package com.gourmich.controller;

import com.gourmich.dto.FavoriteDTO;
import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import com.gourmich.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<?> getFavorites() {
        try {
            List<FavoriteDTO> favorites = favoriteService.getFavoritesForCurrentUser();
            return ResponseEntity.ok(favorites);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne lors de la récupération des favoris");
        }
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestParam Long recipeId) {
        try {
            FavoriteDTO dto = favoriteService.toggleFavoriteForCurrentUser(recipeId);

            if (dto == null) {
                return ResponseEntity.ok("Unfav recipe");
            } else {
                return ResponseEntity.ok(dto);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne lors du toggle du favori");
        }
    }

    @GetMapping("/is-favorite/{recipeId}")
    public ResponseEntity<?> isFavorite(@PathVariable Long recipeId) {
        try {
            boolean isFavorite = favoriteService.isAlreadyFavoriteForCurrentUser(recipeId);
            return ResponseEntity.ok(isFavorite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne lors de la vérification du favori");
        }
    }


}