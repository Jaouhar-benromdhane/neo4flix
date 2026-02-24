# Rating Service

Microservice Spring Boot responsable de la gestion des notations de films.

## Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | `/ratings/movie/{movieId}` | Notes d'un film |
| GET | `/ratings/user/{userId}` | Notes d'un utilisateur |
| GET | `/ratings/{id}` | Détail d'une note |
| POST | `/ratings` | Noter un film |
| PUT | `/ratings/{id}` | Modifier une note |
| DELETE | `/ratings/{id}` | Supprimer une note |

## Modèle Neo4j

```cypher
// Créer une note
MATCH (u:User {id: $userId}), (m:Movie {id: $movieId})
MERGE (u)-[r:RATED]->(m)
SET r.score = $score, r.ratedAt = datetime()
RETURN r

// Moyenne des notes d'un film
MATCH (:User)-[r:RATED]->(m:Movie {id: $movieId})
RETURN avg(r.score) as averageRating, count(r) as totalRatings
```

## Variables d'environnement

```env
SPRING_NEO4J_URI=bolt://neo4j:7687
SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
SPRING_NEO4J_AUTHENTICATION_PASSWORD=neo4flix123
JWT_SECRET=your_secret_key
```
