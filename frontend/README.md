# Frontend — Neo4flix Angular App

Application web Angular 17 pour le moteur de recommandation Neo4flix.

## Pages

| Route | Composant | Description |
|-------|-----------|-------------|
| `/login` | AuthComponent | Page de connexion |
| `/register` | RegisterComponent | Page d'inscription |
| `/` | HomeComponent | Accueil — liste des films |
| `/movies/:id` | MovieDetailComponent | Détail d'un film |
| `/movies/:id/rate` | RatingComponent | Notation d'un film |
| `/recommendations` | RecommendationsComponent | Recommandations personnalisées |
| `/watchlist` | WatchlistComponent | Ma liste de films |
| `/search` | SearchComponent | Résultats de recherche |
| `/profile` | ProfileComponent | Profil utilisateur |

## Features
- Authentification JWT avec intercepteur HTTP
- 2FA : saisie du code TOTP
- Recherche multi-critères en temps réel
- Composant étoiles pour notation
- Lazy loading des modules
- Guards de routes (AuthGuard, AdminGuard)

## Installation

```bash
cd frontend
npm install
ng serve          # dev : http://localhost:4200
ng build --prod   # production build (dans dist/)
```

## Variables d'environnement

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```
