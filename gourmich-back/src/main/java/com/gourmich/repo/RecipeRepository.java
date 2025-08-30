package com.gourmich.repo;

import com.gourmich.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    boolean existsByTitleAndAuthorId(String title, Long authorId);

    List<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredients " +
            "LEFT JOIN FETCH r.author " +
            "ORDER BY r.createdAt DESC")
    List<Recipe> findLatestWithRelations(Pageable pageable);
}
