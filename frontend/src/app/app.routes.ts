import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/movies', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
  { path: 'movies', loadComponent: () => import('./pages/movies/movies.component').then(m => m.MoviesComponent) },
  { path: 'movies/:id', loadComponent: () => import('./pages/movie-detail/movie-detail.component').then(m => m.MovieDetailComponent) },
  { path: 'recommendations', loadComponent: () => import('./pages/recommendations/recommendations.component').then(m => m.RecommendationsComponent) },
  { path: 'my-ratings', canActivate: [authGuard], loadComponent: () => import('./pages/my-ratings/my-ratings.component').then(m => m.MyRatingsComponent) },
  { path: 'watchlist', canActivate: [authGuard], loadComponent: () => import('./pages/watchlist/watchlist.component').then(m => m.WatchlistComponent) },
  { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent) },
  { path: 'oauth2-callback', loadComponent: () => import('./pages/oauth2-callback/oauth2-callback.component').then(m => m.OAuth2CallbackComponent) },
  { path: '**', redirectTo: '/movies' }
];

