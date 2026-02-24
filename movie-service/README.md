# Movie Service

Microservice Spring Boot responsable de la gestion des films.

## Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | `/movies` | Liste paginée des films |
| GET | `/movies/{id}` | Détail d'un film |
| GET | `/movies/search` | Recherche (params: title, genre, year) |
| POST | `/movies` | Créer un film (Admin) |
| PUT | `/movies/{id}` | Modifier un film (Admin) |
| DELETE | `/movies/{id}` | Supprimer un film (Admin) |

## Variables d'environnement

```env
SPRING_NEO4J_URI=bolt://neo4j:7687
SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
SPRING_NEO4J_AUTHENTICATION_PASSWORD=neo4flix123
JWT_SECRET=your_secret_key
```

## Build & Run

```bash
# Local
./mvnw spring-boot:run

# Docker
docker build -t neo4flix-movie-service .
```
