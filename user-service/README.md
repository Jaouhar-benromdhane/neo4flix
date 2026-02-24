# User Service

Microservice Spring Boot responsable de la gestion des utilisateurs, de l'authentification JWT et de la 2FA.

## Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| POST | `/auth/register` | Créer un compte |
| POST | `/auth/login` | Connexion (retourne JWT) |
| POST | `/auth/refresh` | Rafraîchir le token JWT |
| POST | `/auth/2fa/setup` | Configurer la 2FA |
| POST | `/auth/2fa/verify` | Vérifier le code 2FA |
| GET | `/users/{id}` | Profil utilisateur |
| PUT | `/users/{id}` | Modifier le profil |
| DELETE | `/users/{id}` | Supprimer le compte |
| GET | `/users/{id}/watchlist` | Obtenir la watchlist |
| POST | `/users/{id}/watchlist/{movieId}` | Ajouter à la watchlist |
| DELETE | `/users/{id}/watchlist/{movieId}` | Retirer de la watchlist |

## Sécurité

- **JWT** : tokens signés HS256, expiration 24h, refresh token 7j
- **BCrypt** : hachage des mots de passe (strength 12)
- **2FA** : TOTP (RFC 6238) compatible Google Authenticator
- **Politique mots de passe** : min 8 chars, 1 majuscule, 1 chiffre, 1 spécial

## Variables d'environnement

```env
SPRING_NEO4J_URI=bolt://neo4j:7687
SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
SPRING_NEO4J_AUTHENTICATION_PASSWORD=neo4flix123
JWT_SECRET=your_secret_key
JWT_EXPIRATION_MS=86400000
TWO_FACTOR_ENABLED=true
```
