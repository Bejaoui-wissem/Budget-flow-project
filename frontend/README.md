# BudgetFlow – Frontend React

Interface React (Vite) qui utilise l'API du backend Java : **connexion, inscription, CRUD des dépenses et des catégories**.

Pages : Connexion · Inscription · Dépenses (ajout, modification, suppression, recherche, filtres, tri, total) · Catégories (ajout, modification, suppression).
Interface responsive (menu latéral, menu mobile), notifications, confirmation avant suppression, thème clair/sombre automatique.

## Prérequis
- Node.js 18 ou plus (`node -v`)
- Le **backend Java démarré** sur `http://localhost:8080` (voir son README)

## Lancer (VS Code ou IntelliJ, dans un terminal)
```bash
npm install
npm run dev
```
Ouvrir **http://localhost:5173** : créer un compte, se connecter, gérer ses dépenses.

Le serveur de développement redirige automatiquement `/api` vers `http://localhost:8080` (voir `vite.config.js`), donc rien d'autre à configurer.

## Autre adresse pour le backend
Créer un fichier `.env.local` à la racine :
```
VITE_API_URL=http://mon-serveur:8080/api
```
(le backend accepte déjà les requêtes d'autres origines).

## Version de production
```bash
npm run build      # génère le dossier dist/
npm run preview    # test local de la version générée
```

## Structure
```
src/
├── main.jsx, App.jsx           démarrage et routes (pages protégées par la connexion)
├── api.js                      appels HTTP + jeton JWT
├── context/                    AuthContext (session), ToastContext (notifications)
├── components/                 Layout, Modal, ConfirmDialog
├── pages/                      Login, Register, Expenses, Categories
└── styles.css
```

## Dépannage
- **« Impossible de joindre le serveur »** : le backend n'est pas démarré.
- **Session expirée** : le jeton dure 2 heures, il suffit de se reconnecter.
- **Port 5173 occupé** : Vite choisit automatiquement le port suivant, utiliser l'adresse affichée dans le terminal.
