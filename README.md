# 🎬 Neo4flix — Movie Recommendation Engine

> A movie recommendation platform built with **Neo4j**, **Spring Boot**, **Angular**, **Microservices** and **Docker**.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17-red)](https://angular.io/)
[![Neo4j](https://img.shields.io/badge/Neo4j-5.x-blue)](https://neo4j.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://www.docker.com/)

---

## 📌 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Team](#team)

---

## Overview

Neo4flix is a full-stack movie recommendation engine that leverages the power of **graph databases** (Neo4j) to deliver intelligent, relationship-based movie recommendations. Users can browse movies, rate them, receive personalized recommendations, and manage a watchlist.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Angular Frontend                    │
└────────────────────────┬────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │     API Gateway      │
              └──────────┬──────────┘
          ┌──────────────┼──────────────┬──────────────┐
          ▼              ▼              ▼              ▼
   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
   │  Movie   │  │  User    │  │  Rating  │  │Recommendation│
   │ Service  │  │ Service  │  │ Service  │  │   Service    │
   └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬───────┘
        └─────────────┴─────────────┴────────────────┘
                             │
                    ┌────────▼────────┐
                    │   Neo4j Graph   │
                    │    Database     │
                    └─────────────────┘
```

---

## Microservices

| Service | Port | Responsibility |
|---------|------|---------------|
| `api-gateway` | 8080 | Route requests, load balancing |
| `movie-service` | 8081 | CRUD movies, movie data |
| `user-service` | 8082 | CRUD users, auth (JWT/OAuth2), 2FA |
| `rating-service` | 8083 | CRUD ratings |
| `recommendation-service` | 8084 | Graph-based recommendation algorithms |
| `frontend` | 4200 | Angular web application |
| `neo4j` | 7474/7687 | Graph database |

---

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 3.x**
- **Spring Data Neo4j** (OGM — Object Graph Mapping)
- **Spring Security** + **JWT** + **OAuth 2.0**
- **Spring Cloud Gateway** (API Gateway)
- **Neo4j Graph Algorithms** (recommendation engine)
- **Cypher Query Language**

### Frontend
- **Angular 17**
- **Angular Material** (UI components)
- **HTTPS / SSL**

### Database
- **Neo4j 5.x** (graph database)
- Nodes: `Movie`, `User`, `Genre`
- Relationships: `RATED`, `WATCHED`, `SAVED`, `RECOMMENDED`

### DevOps
- **Docker** + **Docker Compose**
- **GitHub** (branches: `main`, `develop`, `feature/jaouhar`, `feature/alkzim`)

---

## Getting Started

### Prerequisites
- Docker & Docker Compose installed
- Node.js 18+ (for local Angular dev)
- Java 17+ (for local Spring dev)

### Run with Docker

```bash
# Clone the repository
git clone https://github.com/Jaouhar-benromdhane/neo4flix.git
cd neo4flix

# Start all services
docker-compose up --build

# Access the app
# Frontend:  http://localhost:4200
# API Gateway: http://localhost:8080
# Neo4j Browser: http://localhost:7474
```

### Default Credentials (dev)
| Service | User | Password |
|---------|------|----------|
| Neo4j   | neo4j | neo4flix123 |
| App Admin | admin@neo4flix.com | Admin@123 |

---

## Git Workflow

```
main          ← stable, production-ready
  └── develop ← integration branch
        ├── feature/jaouhar  ← Jaouhar's work
        └── feature/alkzim   ← Alkzim's work
```

**Convention de commits :**
```
feat(movie-service): add recommendation endpoint
fix(user-service): correct JWT expiration
docs: update README
test(rating-service): add unit tests
docker: add neo4j health check
```

---

## Team

| Member | GitHub | Role |
|--------|--------|------|
| Jaouhar Ben Romdhane | [@Jaouhar-benromdhane](https://github.com/Jaouhar-benromdhane) | Backend microservices + DevOps |
| Alkzim | [@Alkzim974](https://github.com/Alkzim974) | Frontend + Backend collaboration |

---

## Project Status

- [ ] Phase 1 — Data modeling (Neo4j schema)
- [ ] Phase 2 — Microservices development (Spring Boot)
- [ ] Phase 3 — Frontend development (Angular)
- [ ] Phase 4 — Security (JWT, OAuth2, 2FA, HTTPS)
- [ ] Phase 5 — Docker deployment
- [ ] Phase 6 — Testing (unit, integration, security, stress)
