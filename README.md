# BookHub

Application de gestion de la bibliothèque du Quartier Solidaire : catalogue, emprunts, réservations et avis.

## Liens

| Lien | URL (en local) |
| --- | --- |
| Application Angular | http://localhost:4200 |
| API | http://localhost:8080/api |
| Documentation de l'API (Swagger UI) | http://localhost:8080/swagger-ui.html |
| Description OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

## Lancer le projet

### Backend (Spring Boot, Java 21)

1. Copier `backend/.env.example` en `backend/.env` et renseigner la connexion Neon et `JWT_SECRET`.
2. Dans `backend` : `./gradlew bootRun`

Au démarrage, Flyway crée le schéma de la base et, avec le profil `dev`, les données de test.

### Frontend (Angular 21)

Dans `frontend` : `npm ci` puis `npm start`

## Comptes de test

Mot de passe de tous les comptes : `BookHub!2026a`

| Rôle | Email |
| --- | --- |
| ADMIN | sophie.admin@quartier-solidaire.fr |
| LIBRARIAN | ahmed.biblio@quartier-solidaire.fr |
| USER | marie.lectrice@example.fr |

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

## Tests

Dans `backend` : `./gradlew test`

Dans `frontend` : `npm test -- --watch=false`

Le script SQL de référence (Neon) est dans `docs/BookHub – Script SQL Neon (PostgreSQL).sql`.
