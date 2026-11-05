# BookHub

Application de gestion de la bibliothèque du Quartier Solidaire : catalogue, emprunts, réservations, avis et tableaux de bord par rôle.

Dépôt : https://github.com/Gryfs/BookHub

## Prérequis

| Outil | Version |
| --- | --- |
| JDK | 21 |
| Node.js | 20+ (npm 11 recommandé) |
| PostgreSQL | Neon (PostgreSQL 16+) ou instance compatible |
| Git | pour cloner le dépôt |

Générer un secret JWT (256 bits) :

```bash
openssl rand -base64 32
```

## Structure

| Dossier | Contenu |
| --- | --- |
| `backend/` | API Spring Boot 3, Flyway, tests JUnit |
| `frontend/` | Application Angular 21 |
| `docs/` | Maquettes (`docs/maquettes/`), script SQL Neon de référence |
| `docs/BookHub – Script SQL Neon (PostgreSQL).sql` | Schéma + fonctions + seed manuel (jury / reprise sur Neon Console) |

En local, le **schéma et les données de test** sont appliqués par **Flyway** : migrations dans `backend/src/main/resources/db/migration/` et seed dev dans `backend/src/main/resources/db/dev/V2__donnees_test.sql` (profil `dev`).

## Liens

| Lien | URL (en local) |
| --- | --- |
| Application Angular | http://localhost:4200 |
| API | http://localhost:8080/api |
| Documentation de l'API (Swagger UI) | http://localhost:8080/swagger-ui.html |
| Description OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

## Lancer le projet

### Backend (Spring Boot, Java 21)

1. Copier `backend/.env.example` en `backend/.env`.
2. Renseigner `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (connexion Neon).
3. Optionnel : `CORS_ALLOWED_ORIGINS` (défaut `http://localhost:4200`).
4. Dans `backend` : `./gradlew bootRun`

Au démarrage, Flyway crée le schéma et, avec le profil `dev`, charge les comptes et livres de démonstration.

### Frontend (Angular 21)

Dans `frontend` :

```bash
npm ci
npm start
```

L'API est configurée dans `frontend/src/environments/environment.development.ts` (`http://localhost:8080/api`).

## Comptes de test

Mot de passe de tous les comptes : `BookHub!2026a`

| Rôle | Email |
| --- | --- |
| ADMIN | sophie.admin@quartier-solidaire.fr |
| LIBRARIAN | ahmed.biblio@quartier-solidaire.fr |
| USER | marie.lectrice@example.fr |

Autres lecteurs seed : `lucas.lecteur@example.fr`, `ines.lectrice@example.fr` (emprunts / retards / réservations pour les scénarios de démo).

## Tester l'API avec Swagger

1. Ouvrir http://localhost:8080/swagger-ui.html
2. Appeler `POST /api/auth/login` avec un compte de test et copier le `token` de la réponse.
3. Cliquer sur **Authorize**, coller le jeton puis valider.
4. Les routes protégées (cadenas) sont alors utilisables.

## Tableaux de bord (US-DASH)

| Rôle | Page | API principale |
| --- | --- | --- |
| USER | http://localhost:4200/dashboard | `GET /api/dashboard/reader` |
| LIBRARIAN | http://localhost:4200/librarian | `GET /api/librarian/dashboard` |
| ADMIN | http://localhost:4200/admin | `GET /api/admin/dashboard`, `GET /api/admin/users`, `PUT /api/admin/users/{id}/role` |

Les indicateurs biblio s'appuient sur les fonctions SQL `fn_stats_bibliotheque` et `fn_top_livres`. Le graphique mensuel (`fn_emprunts_par_mois`) n'est pas exposé en v1.

## Tests et couverture

### Backend

```bash
cd backend
./gradlew test                  # tests JUnit
./gradlew jacocoTestReport      # rapport HTML : build/reports/jacoco/test/html/index.html
./gradlew check                 # tests + seuil JaCoCo ≥ 20 %
```

Tests métier notables : `AuthServiceTest`, `LoanServiceTest`, contrôleurs (`LoanControllerTest`, `ReservationControllerTest`, etc.).

### Frontend

```bash
cd frontend
npm test -- --watch=false
npm test -- --watch=false --coverage   # rapport dans coverage/frontend
```

Couverture cible formateur : **≥ 20 %** (guards, auth, catalogue, profil, dashboards).

## Sécurité (rappel)

- Mots de passe hashés (BCrypt, coût 12), JWT 24 h.
- Rôles : `USER` < `LIBRARIAN` < `ADMIN` (Spring Security + guards Angular).
- Secrets uniquement dans `backend/.env` (non versionné).
