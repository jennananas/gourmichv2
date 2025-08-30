import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Recipe } from '../../services/recipe.service';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-recipe-card',
  standalone: true,
  imports: [ButtonModule, CardModule, ChipModule, CommonModule, ConfirmDialogModule],
  templateUrl: './recipe-card.component.html',
  styleUrls: ['./recipe-card.component.css']
})
export class RecipeCardComponent {
  @Input() recipe!: Recipe;
  @Input() favoriteStatus: boolean = false; 
  @Output() toggleFavoriteEvent = new EventEmitter<Recipe>(); 
  @Output() viewDetails = new EventEmitter<Recipe>();

  imageLoadError: boolean = false;

  setDifficultyLabel(difficulty: number): string {
    switch (difficulty) {
      case 1:
      case 2: return 'Easy';
      case 4:
      case 5: return 'Hard';
      default: return 'Medium';
    }
  }

  onToggleFavorite(event: Event) {
    event.stopPropagation(); 
    this.toggleFavoriteEvent.emit(this.recipe);
  }

  onViewDetails() {
    this.viewDetails.emit(this.recipe);
  }
}