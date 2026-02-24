# Cahier des Charges — Neo4flix

**Projet :** Neo4flix — Moteur de recommandation de films  
**Date :** Février 2026  
**Équipe :** Jaouhar Ben Romdhane, Alkzim  
**Version :** 1.0

---

## 1. Contexte et Objectifs

### 1.1 Contexte
Dans le cadre du cours sur les bases de données graphes et les architectures microservices, ce projet vise à développer une plateforme de recommandation de films exploitant les capacités relationnelles de **Neo4j** associées à une architecture **Spring Boot microservices** et un frontend **Angular**.

### 1.2 Objectifs Généraux
- Concevoir un modèle de données graphe adapté à la recommandation de films
- Développer des microservices REST sécurisés (CRUD complet)
- Implémenter un algorithme de recommandation basé sur les algorithmes du **Neo4j Graph Algorithms Library**
- Déployer l'ensemble via **Docker Compose**
- Fournir une interface utilisateur intuitive et sécurisée

---

## 2. Périmètre Fonctionnel

### 2.1 Fonctionnalités Requises

| ID | Fonctionnalité | Priorité |
|----|---------------|----------|
| F01 | Authentification (login / register) | HAUTE |
| F02 | Recherche de films (titre, genre, date, critères) | HAUTE |
| F03 | Affichage des détails d'un film | HAUTE |
| F04 | Notation d'un film (1-5 étoiles) | HAUTE |
| F05 | Recommandations personnalisées | HAUTE |
| F06 | Filtrage des recommandations (genre, date...) | MOYENNE |
| F07 | Watchlist (sauvegarder des films) | MOYENNE |
| F08 | Partage de recommandations | BASSE |
| F09 | Authentification 2FA | HAUTE |
| F10 | Administration (gestion utilisateurs/films) | MOYENNE |

### 2.2 Fonctionnalités Hors Périmètre
- Streaming vidéo réel
- Paiement / abonnement
- Application mobile native

---

## 3. Architecture Technique

### 3.1 Architecture Microservices

L'application est découpée en **4 microservices métier** + 1 API Gateway :

#### 3.1.1 Movie Service (Port 8081)
- **Responsabilités :** CRUD complet des films
- **Endpoints principaux :**
  - `GET /movies` — liste paginée
  - `GET /movies/{id}` — détail d'un film
  - `GET /movies/search?q=` — recherche
  - `POST /movies` — créer un film (admin)
  - `PUT /movies/{id}` — modifier
  - `DELETE /movies/{id}` — supprimer

#### 3.1.2 User Service (Port 8082)
- **Responsabilités :** Gestion des utilisateurs, authentification, 2FA
- **Endpoints principaux :**
  - `POST /auth/register` — inscription
  - `POST /auth/login` — connexion (JWT)
  - `POST /auth/2fa/verify` — vérification 2FA
  - `GET /users/{id}` — profil utilisateur
  - `PUT /users/{id}` — modifier profil
  - `GET /users/{id}/watchlist` — watchlist
  - `POST /users/{id}/watchlist/{movieId}` — ajouter à la watchlist

#### 3.1.3 Rating Service (Port 8083)
- **Responsabilités :** CRUD des notations
- **Endpoints principaux :**
  - `GET /ratings/movie/{movieId}` — notes d'un film
  - `GET /ratings/user/{userId}` — notes d'un utilisateur
  - `POST /ratings` — noter un film
  - `PUT /ratings/{id}` — modifier une note
  - `DELETE /ratings/{id}` — supprimer une note

#### 3.1.4 Recommendation Service (Port 8084)
- **Responsabilités :** Génération de recommandations via algorithmes graphes
- **Algorithmes :**
  - **Collaborative Filtering** : "Les utilisateurs qui ont aimé X ont aussi aimé Y"
  - **Content-Based** : similarité par genre, acteurs, réalisateur
- **Endpoints principaux :**
  - `GET /recommendations/user/{userId}` — recommandations personnalisées
  - `GET /recommendations/movie/{movieId}` — films similaires
  - `GET /recommendations/user/{userId}/filtered?genre=&year=` — avec filtres

### 3.2 Modèle de Données Neo4j

#### Noeuds (Nodes)
```
(:Movie {id, title, releaseDate, synopsis, posterUrl, language})
(:User  {id, email, username, passwordHash, createdAt, twoFactorEnabled})
(:Genre {id, name})
(:Actor {id, name})
(:Director {id, name})
```

#### Relations (Relationships)
```
(:User)-[:RATED {score: 1-5, ratedAt}]->(:Movie)
(:User)-[:WATCHED {watchedAt}]->(:Movie)
(:User)-[:SAVED {savedAt}]->(:Movie)
(:Movie)-[:HAS_GENRE]->(:Genre)
(:Movie)-[:STARS]->(:Actor)
(:Movie)-[:DIRECTED_BY]->(:Director)
(:Movie)-[:SIMILAR_TO {score}]->(:Movie)
```

---

## 4. Exigences Non Fonctionnelles

### 4.1 Sécurité
- Authentification via **JWT** (JSON Web Token) avec refresh token
- Support **OAuth 2.0** (optionnel, Google/GitHub login)
- **2FA** (Two-Factor Authentication) via TOTP (Google Authenticator)
- Communication **HTTPS** (certificat SSL — Let's Encrypt ou auto-signé pour dev)
- Politique de mot de passe : minimum 8 caractères, 1 majuscule, 1 chiffre, 1 caractère spécial
- Hachage des mots de passe avec **BCrypt**

### 4.2 Performance
- Réponse API < 500ms pour les requêtes standards
- Recommandations générées en < 2s
- Pagination obligatoire sur toutes les listes (20 items/page)

### 4.3 Qualité du Code
- Couverture de tests unitaires > 70%
- Tests d'intégration sur chaque microservice
- Documentation API via **Swagger/OpenAPI**

---

## 5. Contraintes Techniques

| Contrainte | Valeur |
|-----------|--------|
| Langage backend | Java 17 |
| Framework backend | Spring Boot 3.x |
| OGM | Spring Data Neo4j |
| Frontend | Angular 17 |
| Base de données | Neo4j 5.x |
| Containerisation | Docker + Docker Compose |
| Authentification | JWT + OAuth2 |
| CI/CD | GitHub (branches protégées) |

---

## 6. Livrables

| Livrable | Date prévue | Responsable |
|---------|-------------|-------------|
| Modèle de données Neo4j | Semaine 1 | Équipe |
| Movie Service + User Service | Semaine 2 | Jaouhar |
| Rating Service + Recommendation Service | Semaine 3 | Alkzim |
| Frontend Angular (pages principales) | Semaine 3-4 | Alkzim |
| Sécurité (JWT, 2FA, HTTPS) | Semaine 4 | Jaouhar |
| Docker Compose complet | Semaine 4 | Jaouhar |
| Tests (unit, integration, security) | Semaine 5 | Équipe |
| Documentation finale | Semaine 5 | Équipe |

---

## 7. Critères d'Acceptation (Audit)

Conformément à l'audit du professeur, le projet sera validé si :

- [x] L'application se lance via `docker-compose up` sans erreur
- [x] Les 4 microservices fonctionnent et répondent correctement
- [x] Le schéma Neo4j est visualisable avec noeuds et relations corrects
- [x] L'authentification JWT/OAuth2 est opérationnelle
- [x] La 2FA est implémentée et fonctionnelle
- [x] La recherche multi-critères fonctionne
- [x] Les recommandations sont pertinentes (algorithme graphe)
- [x] La watchlist est fonctionnelle
- [x] HTTPS est configuré
- [x] Les tests passent sans erreur critique
