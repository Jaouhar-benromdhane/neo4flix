// ─────────────────────────────────────────────────────────────────
// Neo4flix — Script d'initialisation de la base Neo4j
// Exécuter dans Neo4j Browser : http://localhost:7474
// ─────────────────────────────────────────────────────────────────

// 1. Contraintes d'unicité
CREATE CONSTRAINT movie_id IF NOT EXISTS FOR (m:Movie) REQUIRE m.id IS UNIQUE;
CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT user_email IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;
CREATE CONSTRAINT genre_name IF NOT EXISTS FOR (g:Genre) REQUIRE g.name IS UNIQUE;
CREATE CONSTRAINT actor_id IF NOT EXISTS FOR (a:Actor) REQUIRE a.id IS UNIQUE;
CREATE CONSTRAINT director_id IF NOT EXISTS FOR (d:Director) REQUIRE d.id IS UNIQUE;

// 2. Index pour les recherches
CREATE INDEX movie_title IF NOT EXISTS FOR (m:Movie) ON (m.title);
CREATE INDEX movie_year IF NOT EXISTS FOR (m:Movie) ON (m.releaseYear);

// 3. Données de test — Genres
MERGE (:Genre {name: 'Action'});
MERGE (:Genre {name: 'Comedy'});
MERGE (:Genre {name: 'Drama'});
MERGE (:Genre {name: 'Sci-Fi'});
MERGE (:Genre {name: 'Horror'});
MERGE (:Genre {name: 'Romance'});
MERGE (:Genre {name: 'Thriller'});
MERGE (:Genre {name: 'Animation'});

// 4. Données de test — Films
MERGE (m1:Movie {id: '1', title: 'The Matrix', releaseYear: 1999, synopsis: 'A computer hacker learns about the true nature of reality.', posterUrl: ''})
  WITH m1 MATCH (g:Genre {name: 'Sci-Fi'}) MERGE (m1)-[:HAS_GENRE]->(g);

MERGE (m2:Movie {id: '2', title: 'Inception', releaseYear: 2010, synopsis: 'A thief enters the dreams of others to steal secrets.', posterUrl: ''})
  WITH m2 MATCH (g:Genre {name: 'Sci-Fi'}) MERGE (m2)-[:HAS_GENRE]->(g);

MERGE (m3:Movie {id: '3', title: 'The Dark Knight', releaseYear: 2008, synopsis: 'Batman faces the Joker in Gotham City.', posterUrl: ''})
  WITH m3 MATCH (g:Genre {name: 'Action'}) MERGE (m3)-[:HAS_GENRE]->(g);

// 5. Utilisateur admin de test (mot de passe hashé en prod)
MERGE (:User {id: 'admin-001', email: 'admin@neo4flix.com', username: 'admin', role: 'ADMIN', createdAt: datetime()});
