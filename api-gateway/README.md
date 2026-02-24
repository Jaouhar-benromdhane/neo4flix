# API Gateway

Spring Cloud Gateway — Point d'entrée unique de toute l'application.

## Configuration des routes

| Route | Service cible | Path |
|-------|--------------|------|
| `/api/movies/**` | movie-service:8081 | `/movies/**` |
| `/api/users/**` | user-service:8082 | `/users/**` |
| `/api/auth/**` | user-service:8082 | `/auth/**` |
| `/api/ratings/**` | rating-service:8083 | `/ratings/**` |
| `/api/recommendations/**` | recommendation-service:8084 | `/recommendations/**` |

## Fonctionnalités
- Routage dynamique vers les microservices
- Validation JWT sur toutes les routes protégées
- CORS configuré pour le frontend Angular
- Rate limiting (anti-abus)
- Logging centralisé

## Variables d'environnement

```env
MOVIE_SERVICE_URL=http://movie-service:8081
USER_SERVICE_URL=http://user-service:8082
RATING_SERVICE_URL=http://rating-service:8083
RECOMMENDATION_SERVICE_URL=http://recommendation-service:8084
JWT_SECRET=your_secret_key
```
