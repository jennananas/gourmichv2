import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { MessageService } from 'primeng/api';
import { Recipe, RecipeService } from '../../services/recipe.service';
import { AuthService } from '../../services/auth.service';
import { RecipeCardComponent } from '../../components/recipe-card/recipe-card.component';
import { FavoriteManagerService } from '../../services/favorite-manager.service';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: true,
  imports: [ToastModule,CommonModule, ButtonModule, RouterLink, ChipModule, RecipeCardComponent],
  providers: [MessageService]
})
export class HomeComponent implements OnInit {
  latestRecipes: Recipe[] = [];
  isLoggedIn: boolean = false;

  constructor(
    private router: Router,
    private recipeService: RecipeService,
    private authService: AuthService,
    private favoriteManager: FavoriteManagerService
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();

    this.recipeService.getLatestRecipes(3).subscribe({
      next: (data) => {
        this.latestRecipes = data;
        this.favoriteManager.initializeFavorites(this.latestRecipes);
      },
      error: (err) => console.error('Erreur récupération dernières recettes', err)
    });
  }

  toggleFavorite(recipe: Recipe) {
    this.favoriteManager.toggleFavorite(recipe.id);
  }

  getRecipeDetails(recipe: Recipe) {
    this.router.navigate(['/recipe', recipe.id]);
  }

  getFavoriteStatus(recipeId: number): boolean {
    return this.favoriteManager.getFavoriteStatus(recipeId);
  }
}