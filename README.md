# BudgetFlow

Application web de suivi de budget personnel.

```
budgetflow/
├── backend/    API Java (JDBC + MySQL, BCrypt, JWT) : authentification + CRUD dépenses et catégories
├── frontend/   Interface React (Vite)
└── .vscode/    configuration VS Code (lancement du backend)
```

## Démarrage rapide (VS Code : ouvrir le dossier `budgetflow`)

**1. Base de données** (MySQL 8 démarré)
```bash
mysql -u root -p < backend/src/main/resources/database.sql
```

**2. Backend** : dans `.vscode/launch.json`, remplacer `VOTRE_MOT_DE_PASSE_MYSQL`, puis ouvrir
`backend/src/main/java/tn/enicarthage/budgetflow/Main.java` et cliquer sur *Run* (ou F5).
Console attendue : `BudgetFlow demarre : http://localhost:8080`

**3. Frontend** (autre terminal)
```bash
cd frontend
npm install
npm run dev
```
Ouvrir **http://localhost:5173** : créer un compte, puis gérer ses dépenses et ses catégories.

## IntelliJ IDEA
Ouvrir `backend/pom.xml` (*Open as Project*) pour le Java, et le dossier `frontend` pour React.
Dans la configuration de lancement de `Main`, renseigner les variables d'environnement
`DB_USER=root;DB_PASSWORD=votre_mot_de_passe`.

Détails : `backend/README.md` et `frontend/README.md`.
