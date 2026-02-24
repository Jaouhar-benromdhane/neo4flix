# Recommendation Service

Microservice Spring Boot responsable de la génération de recommandations de films via les algorithmes de graphes Neo4j.

## Algorithmes Implémentés

### 1. Collaborative Filtering
> "Les utilisateurs qui ont aimé les mêmes films que toi ont aussi aimé X"

```cypher
MATCH (u:User {id: $userId})-[r1:RATED]->(m:Movie)<-[r2:RATED]-(other:User)
WHERE r1.score >= 4 AND r2.score >= 4
WITH other, count(m) AS commonMovies
ORDER BY commonMovies DESC
LIMIT 10
MATCH (other)-[r:RATED]->(rec:Movie)
WHERE NOT (u)-[:RATED]->(rec) AND r.score >= 4
RETURN rec, count(*) AS score
ORDER BY score DESC
LIMIT 20
```

### 2. Content-Based Filtering
> "Ce film partage les mêmes genres/acteurs que ceux que tu as aimés"

```cypher
MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie)-[:HAS_GENRE]->(g:Genre)
WHERE r.score >= 4
WITH u, g, count(m) AS genreWeight
MATCH (g)<-[:HAS_GENRE]-(rec:Movie)
WHERE NOT (u)-[:RATED]->(rec)
RETURN rec, sum(genreWeight) AS relevanceScore
ORDER BY relevanceScore DESC
LIMIT 20
```

## Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | `/recommendations/user/{userId}` | Recommandations personnalisées |
| GET | `/recommendations/movie/{movieId}` | Films similaires |
| GET | `/recommendations/user/{userId}/filtered` | Avec filtres (genre, year) |

## Variables d'environnement

```env
SPRING_NEO4J_URI=bolt://neo4j:7687
SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
SPRING_NEO4J_AUTHENTICATION_PASSWORD=neo4flix123
JWT_SECRET=your_secret_key
```
