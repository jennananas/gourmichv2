package com.gourmich.repo;

import com.gourmich.models.Ingredient;
import com.gourmich.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long>  {

    void deleteAllByRecipe(Recipe recipe);
}
