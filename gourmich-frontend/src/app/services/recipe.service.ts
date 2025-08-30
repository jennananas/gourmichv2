import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment.prod';


export interface Recipe {
  id: number;
  title: string;
  description: string;
  authorUsername: string;
  ingredients: Ingredient[]
  instructions: string;
  imageUrl?: string;
  cookingTime: number;
  difficulty: number;
  category: string;
}

export interface Ingredient {
  name: string;
  unit: string;
  quantity: number;
}

export interface Favorite {
  id: number;
  recipeId: number;
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class RecipeService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  private recipesUrl = `${environment.apiUrl}recipes`;
  private favoritesUrl = `${environment.apiUrl}favorites`;

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(this.recipesUrl);
  }

  getRecipeById(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.recipesUrl}/by-id/${id}`);
  } 

  createRecipe(recipe: Recipe): Observable<Recipe> {
  return this.http.post<Recipe>(`${this.recipesUrl}`, recipe);
}

  deleteRecipe(id: number): Observable<any> {
  return this.http.delete(`${this.recipesUrl}/by-id/${id}`);
}

  updateRecipe(id: number, recipe: Recipe): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.recipesUrl}/by-id/${id}`, recipe);
  }

  getFavorites(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(this.favoritesUrl, {
      headers: this.getAuthHeaders()
    });
  }
  
  toggleFavorite(recipeId: number): Observable<string> {
    return this.http.post(`${this.favoritesUrl}/toggle?recipeId=${recipeId}`, {}, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  isFavorite(recipeId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.favoritesUrl}/is-favorite/${recipeId}`, {
      headers: this.getAuthHeaders()
    });
  }

  
  
  getLatestRecipes(nbOfRecipe: number = 3): Observable<any[]> {
  return this.http.get<any[]>(`${this.recipesUrl}/latest?n=${nbOfRecipe}`);
}
}
