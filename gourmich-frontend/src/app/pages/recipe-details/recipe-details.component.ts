import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RecipeService, Recipe } from '../../services/recipe.service';
import { ChipModule } from 'primeng/chip';
import { CommonModule } from '@angular/common';
import { AccordionModule } from 'primeng/accordion';
import { ButtonModule } from 'primeng/button';
import { DifficultyLabelPipe } from '../../pipes/difficultyLabelPipe';
import { AvatarModule } from 'primeng/avatar';
import { FirstLetterPipe } from '../../pipes/firstLetterPipe';
import { TabsModule } from 'primeng/tabs';
import { AuthService } from '../../services/auth.service';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { FavoriteManagerService } from '../../services/favorite-manager.service';

@Component({
  selector: 'app-recipe-details',
  standalone: true,
  imports: [
    ToastModule, ConfirmDialogModule, TooltipModule, ChipModule, CommonModule,
    AccordionModule, ButtonModule, RouterLink, DifficultyLabelPipe, AvatarModule,
    FirstLetterPipe, TabsModule
  ],
  templateUrl: './recipe-details.component.html',
  styleUrls: ['./recipe-details.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class RecipeDetailsComponent implements OnInit {
  recipe: Recipe = {} as Recipe;
  imageLoadError: Record<number, boolean> = {};
  currentUser: string | null = null;
  isFavorite: boolean = false;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private recipeService: RecipeService,
    private router: Router,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private favoriteManager: FavoriteManagerService
  ) {}

  ngOnInit(): void {
  const recipeId = Number(this.route.snapshot.paramMap.get('id'));
  this.currentUser = this.authService.getUsername();

  this.recipeService.getRecipeById(recipeId).subscribe({
    next: (recipe) => {
      this.recipe = recipe;

      if (this.currentUser) {
        this.recipeService.isFavorite(recipe.id).subscribe({
          next: (fav) => this.isFavorite = fav,
          error: (err) => console.error('Erreur vérification favoris', err)
        });
      }
    },
    error: (err) => console.error('Erreur récupération recette', err)
  });
}

  getIngredientsCount(): number {
    return this.recipe?.ingredients?.length ?? 0;
  }

  confirmDelete(recipe: Recipe, event: Event): void {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `
      <div class="text-center flex flex-col items-center">
        <i class="pi pi-exclamation-triangle !text-4xl text-red-600 mb-2"></i>
        <p>You are about to delete the recipe "<strong>${recipe.title}</strong>".</p>
        <p>Are you sure?</p>
      </div>
    `,
      accept: () => {
        this.deleteRecipe(recipe);
      },
      reject: () => {
        this.messageService.add({ severity: 'info', summary: 'Annulé', detail: 'Suppression annulée' });
      },
      acceptLabel: 'Yes, delete',
      rejectLabel: 'No, cancel',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-secondary',
    });
  }

  deleteRecipe(recipe: Recipe): void {
    this.recipeService.deleteRecipe(recipe.id).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Recette supprimée' });
        setTimeout(() => {
        this.router.navigate(['/recipes']);
      }, 1500);
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer la recette' });
        console.error(err);
      }
    });
  }

toggleFavorite(recipe: Recipe) {
  try {
    this.favoriteManager.toggleFavorite(recipe.id);
    this.isFavorite = !this.isFavorite;
  } catch (err) {
    console.error('Erreur toggle favori', err);
  }
}
}