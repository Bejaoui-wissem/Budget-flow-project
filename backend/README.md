# BudgetFlow – Backend Java (authentification + CRUD)

API REST en Java pur (sans framework) : serveur HTTP du JDK, JDBC, MySQL, mots de passe BCrypt, jetons JWT.

**Fonctionnalités :**
- Authentification : inscription, connexion, utilisateur courant (`/api/auth/...`).
- CRUD des **dépenses** et des **catégories** (créer, consulter, modifier, supprimer), avec recherche, filtres et tri.
- Chaque utilisateur ne voit et ne modifie que ses propres données.
- Une page de test est servie sur `http://localhost:8080/`.

## 1. Prérequis
- JDK 17 ou plus (`java -version`)
- Maven (`mvn -version`) — ou laisser VS Code l'utiliser via l'extension Java
- MySQL 8 démarré
- VS Code + **Extension Pack for Java** (recommandée automatiquement à l'ouverture du dossier)

## 2. Créer la base de données
```bash
mysql -u root -p < src/main/resources/database.sql
```
(ou MySQL Workbench : *File → Run SQL Script…* puis choisir `database.sql`)

## 3. Configurer l'accès à MySQL
Dans `.vscode/launch.json` (à la racine du dossier budgetflow), remplacer `VOTRE_MOT_DE_PASSE_MYSQL` par votre mot de passe MySQL
(et `DB_USER` si ce n'est pas `root`). Variables disponibles :

| Variable | Défaut |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/budgetflow?...` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vide)* |
| `JWT_SECRET` | secret de développement (à changer) |
| `PORT` | `8080` |

## 4. Lancer
**Dans VS Code :** ouvrir le dossier du projet, attendre l'import Maven, puis ouvrir `Main.java`
et cliquer sur *Run* (ou F5 avec la configuration « BudgetFlow (Main) »).

**En terminal :**
```bash
# Windows PowerShell
$env:DB_PASSWORD="votre_mot_de_passe"
mvn compile exec:java

# Linux / macOS
DB_PASSWORD=votre_mot_de_passe mvn compile exec:java
```
Console attendue :
```
Connexion a la base de donnees : OK
BudgetFlow demarre : http://localhost:8080
```
Ouvrir ensuite **http://localhost:8080** : créer un compte, se connecter, ajouter / modifier / supprimer des dépenses.

Jar exécutable : `mvn package` puis `java -jar target/budgetflow-1.0.0-all.jar`.

## 5. Tester l'API
Installer l'extension **REST Client**, ouvrir `docs/tests.http` et cliquer sur *Send Request* au-dessus de chaque requête.

## 6. Routes
Toutes les routes sauf `register` et `login` exigent l'en-tête `Authorization: Bearer <jeton>`.

| Méthode | Route | Action |
|---|---|---|
| POST | `/api/auth/register` | Créer un compte (retourne un jeton) |
| POST | `/api/auth/login` | Se connecter (retourne un jeton) |
| GET | `/api/auth/me` | Utilisateur connecté |
| GET | `/api/categories` | Lister (10 par défaut + personnelles) |
| GET | `/api/categories/{id}` | Consulter |
| POST | `/api/categories` | Créer `{nom, description}` |
| PUT | `/api/categories/{id}` | Modifier (personnelles uniquement) |
| DELETE | `/api/categories/{id}` | Supprimer (si aucune dépense) |
| GET | `/api/expenses` | Lister. Filtres : `categoryId`, `from`, `to`, `q`, `sort=date\|montant`, `order=asc\|desc` |
| GET | `/api/expenses/{id}` | Consulter |
| POST | `/api/expenses` | Créer `{montant, date, description, modePaiement, categoryId}` |
| PUT | `/api/expenses/{id}` | Modifier |
| DELETE | `/api/expenses/{id}` | Supprimer |

Codes : 200/201/204 succès · 400 données invalides · 401 non authentifié · 404 introuvable · 409 conflit (e-mail déjà utilisé, catégorie non vide).

## 7. Structure
```
src/main/java/tn/enicarthage/budgetflow
├── config/DatabaseConnection.java   connexion JDBC
├── controller/                      routes (Auth, Category, Expense)
├── dao/                             accès MySQL (PreparedStatement)
├── dto/                             objets de requête / réponse
├── exception/ApiException.java      erreurs HTTP
├── model/                           User, Category, Expense
├── router/                          Router, Handler, RequestContext, Response, StaticHandler
├── service/                         règles métier et validation
├── util/                            Json, JwtUtil, PasswordUtil, Validator
├── ApiServer.java                   assemblage et démarrage du serveur
└── Main.java
src/main/resources
├── database.sql
└── static/index.html                page de test
```

## 8. Dépannage
- **Access denied for user** : mauvais `DB_USER` / `DB_PASSWORD`.
- **Unknown database 'budgetflow'** : `database.sql` n'a pas été exécuté.
- **Communications link failure** : MySQL n'est pas démarré ou le port diffère (`DB_URL`).
- **Address already in use** : changer `PORT`.
- **Accents illisibles dans les catégories** : relancer `database.sql` (il force `utf8mb4`).
