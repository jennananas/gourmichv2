import { Component } from '@angular/core';
import { RecipeListComponent } from "../../recipe-list/recipe-list.component";

@Component({
  selector: 'app-favorites',
  imports: [RecipeListComponent],
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.css'
})
export class FavoritesComponent {
  currentUser = localStorage.getItem('username');

}
