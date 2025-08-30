import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Observable, tap, catchError, of } from 'rxjs';
import { AuthService } from './auth.service';
import { RecipeService } from './recipe.service';

@Injectable({
  providedIn: 'root'
})
export class FavoriteManagerService {
  private favoriteStatus: Record<number, boolean> = {};

  constructor(
    private authService: AuthService,
    private recipeService: RecipeService,
    private router: Router,
    private messageService: MessageService
  ) {}

  initializeFavorites(recipes: any[]): void {
    const isLoggedIn = this.authService.isLoggedIn();

    if (isLoggedIn) {
      this.recipeService.getFavorites().subscribe({
        next: (favorites) => {
          const favSet = new Set(favorites.map((fav: any) => fav.recipeId));
          recipes.forEach(recipe => {
            this.favoriteStatus[recipe.id] = favSet.has(recipe.id);
          });
        },
        error: () => {
          recipes.forEach(recipe => this.favoriteStatus[recipe.id] = false);
        }
      });
    } else {
      recipes.forEach(recipe => this.favoriteStatus[recipe.id] = false);
    }
  }

  getFavoriteStatus(recipeId: number): boolean {
    return this.favoriteStatus[recipeId] || false;
  }

 toggleFavorite(recipeId: number): void {
  if (!this.authService.isLoggedIn()) {
    this.router.navigate(['/login']);
    return;
  }

  this.recipeService.toggleFavorite(recipeId).subscribe({
    next: (response: any) => {
      this.favoriteStatus[recipeId] = response !== 'Unfav recipe';

      this.messageService.add({
        severity: 'success',
        summary: this.favoriteStatus[recipeId] ? 'Ajouté aux favoris' : 'Retiré des favoris',
        detail: this.favoriteStatus[recipeId]
          ? 'La recette est dans vos favoris'
          : 'La recette a été retirée'
      });
    },
    error: (err) => {
      console.error('Erreur lors du toggle', err);
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Impossible de modifier les favoris'
      });
    }
  });
}
}
