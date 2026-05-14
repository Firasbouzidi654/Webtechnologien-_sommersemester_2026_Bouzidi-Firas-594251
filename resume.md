# Résumé de l'Analyse et Corrections du Projet

## Structure Exacte des Dossiers Trouvée

Le projet est organisé en monorepo avec la structure suivante :

```
Cloned/ (racine du projet)
├── backend/ (Spring Boot - Java 21)
│   ├── pom.xml (Maven build)
│   ├── mvnw & mvnw.cmd (Maven Wrapper)
│   ├── .mvn/ (configuration Maven)
│   ├── src/main/resources/
│   │   ├── application.properties (config locale H2)
│   │   └── application-production.properties (config Render PostgreSQL)
│   └── src/main/java/ (code Spring Boot)
├── frontend/ (Vue.js + Vite)
│   ├── package.json
│   ├── src/ (code Vue.js)
│   └── dist/ (build output - généré)
├── render.yaml (configuration Render)
├── .renderignore (ignore frontend/ pour éviter détection Node.js)
├── Dockerfile (build multi-étapes)
└── Documentation (.md files)
```

## Pourquoi le Chemin Actuel vers mvnw et ../frontend Causait une Erreur sur Render

### Problème Principal : Contexte de Build Limité
- **Render rootDir: backend** : Le build se déroule uniquement dans le dossier `backend/`
- **mvnw accessible** : `mvnw` existe dans `backend/`, donc pas d'erreur "No such file"
- **../frontend inaccessible** : Le plugin frontend-maven-plugin utilise `workingDirectory=../frontend`, mais depuis `backend/`, `../frontend` n'existe pas dans le contexte Docker limité
- **npm install échoue** : Le plugin tente d'installer Node.js et npm dans `../frontend`, mais le dossier n'est pas accessible

### Erreur Docker Context
- Docker build avec `rootDir: backend` ne peut pas accéder aux fichiers en dehors de `backend/`
- `COPY . .` copie seulement le contenu de `backend/`, pas `../frontend`

## Correction de la Priorité de Base de Données

### Configuration Actuelle
- **application.properties** (profil par défaut) :
  ```properties
  spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
  ```
  - Utilise H2 en local (développement)
  - Utilise PostgreSQL si `DATABASE_URL` est défini

- **application-production.properties** (profil production sur Render) :
  ```properties
  spring.datasource.url=${DATABASE_URL}
  spring.datasource.username=${DATABASE_USER:}
  spring.datasource.password=${DATABASE_PASSWORD:}
  ```
  - Force l'utilisation de `DATABASE_URL` (pas de fallback H2)
  - Variables d'environnement écrasent toute config locale

### Comment la Priorité Fonctionne
1. **Profil Spring** : `SPRING_PROFILES_ACTIVE=production` sur Render
2. **Variables d'environnement** : `DATABASE_URL` définie sur Render
3. **Override automatique** : Les `${VARIABLE}` remplacent les valeurs par défaut
4. **Sécurité** : Pas de credentials hardcodés, configuration IntelliJ locale ignorée

## Corrections Apportées

### 1. Changement de Stratégie de Build
- **Avant** : Build séparé backend avec plugin Maven pour frontend
- **Après** : Build multi-étapes Docker depuis la racine

### 2. render.yaml Modifié
```yaml
rootDir: .  # Depuis backend/ vers racine
runtime: docker  # Utilisation de Dockerfile
```

### 3. Dockerfile Multi-Étapes Créé
- **Étape 1** : Build frontend avec Node.js
- **Étape 2** : Build backend avec Maven, copie frontend built
- **Étape 3** : Image runtime légère

### 4. pom.xml Ajusté
- Plugin frontend-maven-plugin désactivé (commenté)
- Resources plugin garde la copie (sécurité)

### 5. .renderignore Présent
- Ignore `frontend/` pour éviter détection Node.js par Render

## Résultat
- ✅ Build depuis racine : Accès à `backend/` et `frontend/`
- ✅ Frontend built séparément dans Docker
- ✅ Backend intègre frontend dans JAR
- ✅ Variables d'environnement prioritaires
- ✅ Déploiement Docker propre sur Render
