-- BudgetFlow : script de création de la base (MySQL 8+)
-- Exécution : mysql -u root -p < src/main/resources/database.sql

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS budgetflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE budgetflow;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(50) NOT NULL,
  prenom VARCHAR(50) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  mot_de_passe VARCHAR(255) NOT NULL,
  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(80) NOT NULL,
  description VARCHAR(255),
  date_debut DATE,
  date_fin DATE,
  budget_total DECIMAL(10,2) CHECK (budget_total >= 0),
  statut ENUM('en_cours','termine','annule') NOT NULL DEFAULT 'en_cours',
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  user_id INT NULL,
  project_id INT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS revenues (
  id INT AUTO_INCREMENT PRIMARY KEY,
  montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
  date_revenu DATE NOT NULL,
  source VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expenses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
  date_depense DATE NOT NULL,
  description VARCHAR(255),
  mode_paiement VARCHAR(30),
  user_id INT NOT NULL,
  category_id INT NOT NULL,
  project_id INT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id),
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS budgets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  montant_limite DECIMAL(10,2) NOT NULL CHECK (montant_limite > 0),
  mois TINYINT NOT NULL CHECK (mois BETWEEN 1 AND 12),
  annee SMALLINT NOT NULL,
  user_id INT NOT NULL,
  category_id INT NOT NULL,
  project_id INT NULL,
  revenue_id INT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id),
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
  FOREIGN KEY (revenue_id) REFERENCES revenues(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS alerts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  message VARCHAR(255) NOT NULL,
  type ENUM('proche','attention','depasse') NOT NULL,
  lu BOOLEAN NOT NULL DEFAULT FALSE,
  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  budget_id INT NOT NULL,
  FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE
);

-- Catégories par défaut (user_id NULL = visibles par tous les utilisateurs)
INSERT INTO categories (nom, description, user_id)
SELECT * FROM (
  SELECT 'Alimentation' AS nom, 'Courses, restaurants' AS description, NULL AS user_id UNION ALL
  SELECT 'Transport', 'Taxi, bus, carburant', NULL UNION ALL
  SELECT 'Logement', 'Loyer, charges', NULL UNION ALL
  SELECT 'Études', 'Livres, frais de scolarité', NULL UNION ALL
  SELECT 'Santé', 'Médecin, pharmacie', NULL UNION ALL
  SELECT 'Shopping', 'Vêtements, achats', NULL UNION ALL
  SELECT 'Loisirs', 'Sorties, jeux', NULL UNION ALL
  SELECT 'Abonnements', 'Internet, applications', NULL UNION ALL
  SELECT 'Factures', 'Électricité, eau, téléphone', NULL UNION ALL
  SELECT 'Autre', 'Dépenses diverses', NULL
) AS defaults
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE user_id IS NULL);
