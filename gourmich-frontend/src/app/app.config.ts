import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';
import { ToastModule } from 'primeng/toast';

import { authGuard } from './auth.guard';
import { AuthInterceptor } from './services/auth.interceptor';
import { ConfirmationService, MessageService } from 'primeng/api';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/forgot-pswd/forgot-pswd.component').then((m) => m.ForgotPswdComponent),
  },
  {
    path: 'recipes',
    loadComponent: () =>
      import('./pages/recipes/recipes.component').then((m) => m.RecipesComponent),
  },
  {
    path: 'recipe/:id',
    loadComponent: () =>
      import('./pages/recipe-details/recipe-details.component').then((m) => m.RecipeDetailsComponent),
  },
  {
    path: 'favorites',
    loadComponent: () =>
      import('./pages/favorites/favorites.component').then((m) => m.FavoritesComponent),
    canActivate: [authGuard],
  },
  {
    path: 'create-recipe',
    loadComponent: () =>
      import('./pages/create-recipe-form/create-recipe-form.component').then((m) => m.CreateRecipeFormComponent),
    canActivate: [authGuard],
  },
  {
    path: 'edit-recipe/:id',
    loadComponent: () =>
      import('./pages/create-recipe-form/create-recipe-form.component').then((m) => m.CreateRecipeFormComponent),
    canActivate: [authGuard],
  },
];

// Define preset avec type explicite
const MyPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{orange.50}',
      100: '{orange.100}',
      200: '{orange.200}',
      300: '{orange.300}',
      400: '{orange.400}',
      500: '{orange.500}',
      600: '{orange.600}',
      700: '{orange.700}',
      800: '{orange.800}',
      900: '{orange.900}',
      950: '{orange.950}',
    },
  },
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimationsAsync(),
    importProvidersFrom(ToastModule),
    MessageService,
    ConfirmationService,
    providePrimeNG({
      theme: {
        preset: MyPreset,
      },
    }),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
};