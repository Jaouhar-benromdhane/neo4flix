# Planning — Neo4flix

**Durée estimée :** 5 semaines  
**Équipe :** Jaouhar Ben Romdhane + Alkzim  
**Méthode :** Agile (sprints hebdomadaires) + Git Flow

---

## Vue d'ensemble (Gantt simplifié)

```
Tâche                              | S1 | S2 | S3 | S4 | S5
-----------------------------------|----|----|----|----|----
Setup projet & Git                 | ██ |    |    |    |
Modèle données Neo4j               | ██ |    |    |    |
Movie Service (CRUD)               | ██ | ██ |    |    |
User Service (CRUD + Auth JWT)     |    | ██ | ██ |    |
Rating Service (CRUD)              |    |    | ██ |    |
Recommendation Service (algo)      |    |    | ██ | ██ |
Frontend Angular - Auth pages      |    |    | ██ |    |
Frontend Angular - Film pages      |    |    | ██ | ██ |
Frontend Angular - Recommandations |    |    |    | ██ |
Sécurité (2FA, HTTPS, OAuth2)      |    |    |    | ██ |
Docker Compose (tous services)     |    |    |    | ██ |
Tests unitaires                    |    | ██ | ██ | ██ |
Tests intégration + sécurité       |    |    |    | ██ | ██
Documentation + README final       |    |    |    |    | ██
Revue finale & audit prep          |    |    |    |    | ██
```

---

## Sprint 1 — Setup & Modélisation (Semaine 1)

**Objectif :** Avoir les fondations solides du projet.

### Jaouhar
- [x] Initialiser le repo GitHub avec branches (main, develop, feature/jaouhar, feature/alkzim)
- [ ] Créer le README, CAHIER_DES_CHARGES, PLANNING
- [ ] Setup Docker Compose de base (Neo4j + structure microservices)
- [ ] Configurer Spring Boot project pour `movie-service`
- [ ] Définir le schéma Neo4j (noeuds, relations, contraintes)
- [ ] Charger un dataset de films dans Neo4j (ex: TMDB ou MovieLens)

### Alkzim
- [ ] Setup Angular project (ng new frontend)
- [ ] Installer Angular Material + routing de base
- [ ] Créer les maquettes des pages principales (Figma ou wireframe)
- [ ] Configurer Spring Boot project pour `user-service`

**Commit cible :** `feat: initial project setup with Neo4j schema and microservices skeleton`

---

## Sprint 2 — Movie Service + Auth (Semaine 2)

**Objectif :** API Movie fonctionnelle + authentification de base.

### Jaouhar
- [ ] Implémenter Movie Service : entités, repositories, services, controllers
- [ ] Requêtes Cypher pour recherche avancée (titre, genre, date)
- [ ] Tests unitaires Movie Service (JUnit 5 + Mockito)
- [ ] Configurer API Gateway (Spring Cloud Gateway)
- [ ] JWT : génération et validation du token

### Alkzim
- [ ] Implémenter User Service : register, login, profil
- [ ] Intégrer Spring Security + JWT dans User Service
- [ ] Page login Angular (avec appel API)
- [ ] Page register Angular (validations formulaire)
- [ ] Tests unitaires User Service

**Commit cible :** `feat(movie-service): CRUD + search | feat(user-service): JWT authentication`

---

## Sprint 3 — Rating + Recommendation + Frontend Movies (Semaine 3)

**Objectif :** Notation et premières recommandations + pages films.

### Jaouhar
- [ ] Implémenter Rating Service (CRUD complet)
- [ ] Relation `(:User)-[:RATED]->(:Movie)` dans Neo4j
- [ ] Commencer Recommendation Service (algorithme content-based)
- [ ] Requête Cypher de base : "films similaires par genre"

### Alkzim
- [ ] Page Home Angular (liste films, pagination)
- [ ] Page détail film (titre, genre, date, note moyenne)
- [ ] Page notation d'un film (composant étoiles)
- [ ] Barre de recherche avec filtres
- [ ] Tests Rating Service

**Commit cible :** `feat(rating-service): CRUD ratings | feat(frontend): movie pages + search`

---

## Sprint 4 — Recommandations avancées + Sécurité + Docker (Semaine 4)

**Objectif :** Recommandations intelligentes + application sécurisée + Docker.

### Jaouhar
- [ ] Algorithme collaborative filtering (Neo4j Graph Algorithms)
- [ ] Endpoint recommandations avec filtres (genre, année)
- [ ] Configurer HTTPS (certificat SSL auto-signé pour dev)
- [ ] Implémenter 2FA (TOTP avec Google Authenticator)
- [ ] Docker Compose complet (tous services + Neo4j)
- [ ] Variables d'environnement et secrets Docker

### Alkzim
- [ ] Page recommandations Angular
- [ ] Watchlist feature (backend User Service + frontend)
- [ ] Partage de recommandations (lien)
- [ ] Intégration 2FA dans le frontend
- [ ] Filtres recommandations dans l'UI

**Commit cible :** `feat(recommendation-service): collaborative filtering | feat: 2FA + HTTPS + Docker`

---

## Sprint 5 — Tests, Polish & Audit (Semaine 5)

**Objectif :** Application prête pour l'audit du professeur.

### Jaouhar + Alkzim (en binôme)
- [ ] Tests fonctionnels complets (Postman collection)
- [ ] Tests de sécurité (SQL injection, XSS, tokens expirés)
- [ ] Tests de stress (charge simultanée)
- [ ] Gestion des erreurs et edge cases
- [ ] Documentation Swagger/OpenAPI sur chaque microservice
- [ ] README final mis à jour
- [ ] Nettoyer le code + enlever logs/données sensibles
- [ ] `docker-compose up` testé de zéro sur machine propre

**Commit cible :** `test: full test suite | docs: final documentation | chore: production cleanup`

---

## Répartition des Responsabilités

| Domaine | Responsable Principal | Backup |
|---------|----------------------|--------|
| Neo4j Schema + Cypher | Jaouhar | Alkzim |
| Movie Service | Jaouhar | Alkzim |
| User Service + Auth | Alkzim | Jaouhar |
| Rating Service | Alkzim | Jaouhar |
| Recommendation Service | Jaouhar | Alkzim |
| API Gateway | Jaouhar | — |
| Angular Frontend | Alkzim | Jaouhar |
| Docker + DevOps | Jaouhar | Alkzim |
| Sécurité (JWT, 2FA, HTTPS) | Jaouhar | Alkzim |
| Tests | Les deux | — |

---

## Convention Git (à respecter absolument)

### Branches
```
main          ← merge uniquement depuis develop (code stable)
develop       ← intégration des features validées
feature/jaouhar  ← branche de travail Jaouhar
feature/alkzim   ← branche de travail Alkzim
```

### Format des commits
```
<type>(<scope>): <description courte>

Types : feat | fix | docs | test | docker | style | refactor | chore
```

### Exemples
```bash
git commit -m "feat(movie-service): add Cypher search by genre and date"
git commit -m "fix(user-service): handle expired JWT token correctly"
git commit -m "docs: update planning sprint 2"
git commit -m "docker: add health checks to docker-compose"
git commit -m "test(rating-service): add unit tests for RatingController"
```

### Workflow pour chaque feature
```bash
# 1. Se mettre à jour depuis develop
git checkout develop
git pull origin develop

# 2. Aller sur sa branche de travail
git checkout feature/jaouhar   # ou feature/alkzim

# 3. Merger develop dans sa branche
git merge develop

# 4. Coder + commits réguliers
git add .
git commit -m "feat(...): ..."
git push origin feature/jaouhar

# 5. Quand feature terminée → PR vers develop
# (via GitHub : Pull Request feature/jaouhar → develop)
```
