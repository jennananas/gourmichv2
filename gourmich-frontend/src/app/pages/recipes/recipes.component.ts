import { Component } from '@angular/core';
import { RecipeListComponent } from '../../recipe-list/recipe-list.component';
import { Button } from "primeng/button";
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { RecipeService } from '../../services/recipe.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-recipes',
  imports: [RecipeListComponent, Button, IconFieldModule, InputIconModule, RouterLink],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.css'
})
export class RecipesComponent {

}
