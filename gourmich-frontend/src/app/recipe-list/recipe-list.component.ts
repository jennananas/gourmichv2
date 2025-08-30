import { Component, Input, OnInit } from '@angular/core';
import { Recipe, RecipeService } from '../services/recipe.service';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { RecipeCardComponent } from '../components/recipe-card/recipe-card.component';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-recipe-list',
  imports: [CommonModule, RecipeCardComponent, ButtonModule, RouterLink],
  templateUrl: './recipe-list.component.html',
  styleUrls: ['./recipe-list.component.css']  
})
export class RecipeListComponent implements OnInit {
  @Input() showFavorites: boolean = false;
  currentUser: string | null = null;
  recipes: Recipe[] = [];
  favoriteStatus: Record<number, boolean> = {};

  constructor(
    private router: Router,
    private recipeService: RecipeService,
    private authService: AuthService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getUsername();

    if (this.showFavorites) {
      if (!this.currentUser) return; // si non connecté, ne pas charger les favoris

      this.recipeService.getFavorites().subscribe({
        next: (favorites) => {
          this.recipes = favorites.map((fav: any) => ({
            id: fav.recipeId,
            title: fav.title,
            category: fav.category,
            difficulty: fav.difficulty,
            cookingTime: fav.cookingTime,
            authorUsername: fav.authorUsername,
            description: fav.description,
            ingredients: fav.ingredients,
            instructions: fav.instructions,
            imageUrl: fav.imageUrl
          }));
          this.recipes.forEach(recipe => this.favoriteStatus[recipe.id] = true);
        },
        error: (err) => console.error("Erreur récupération favoris", err)
      });
    } else {
      this.recipeService.getAllRecipes().subscribe({
        next: (recipes) => {
          this.recipes = recipes;

          // Ne récupérer les favoris que si utilisateur connecté
          if (this.currentUser) {
            this.recipeService.getFavorites().subscribe({
              next: (favorites) => {
                const favSet = new Set(favorites.map((fav: any) => fav.recipeId));
                this.recipes.forEach(recipe => {
                  this.favoriteStatus[recipe.id] = favSet.has(recipe.id);
                });
              },
              error: () => {
                this.recipes.forEach(recipe => this.favoriteStatus[recipe.id] = false);
              }
            });
          } else {
            this.recipes.forEach(recipe => this.favoriteStatus[recipe.id] = false);
          }
        },
        error: (err) => console.error("Erreur récupération recettes", err)
      });
    }
  }

  toggleFavorite(recipeId: number): void {
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.recipeService.toggleFavorite(recipeId).subscribe({
      next: (response: any) => {
        this.favoriteStatus[recipeId] = response !== 'Unfav recipe';

        if (this.showFavorites && !this.favoriteStatus[recipeId]) {
          this.recipes = this.recipes.filter(r => r.id !== recipeId);
        }

        this.messageService.add({
          severity: 'success',
          summary: this.favoriteStatus[recipeId] ? 'Ajouté aux favoris' : 'Retiré des favoris',
          detail: this.favoriteStatus[recipeId]
            ? 'La recette est dans vos favoris'
            : 'La recette a été retirée',
        });
      },
      error: (err) => {
        console.error('Erreur lors du toggle', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de modifier les favoris',
        });
      }
    });
  }

  getRecipeDetails(recipeId: number): void {
    this.router.navigate(['/recipe', recipeId]);
  }
}