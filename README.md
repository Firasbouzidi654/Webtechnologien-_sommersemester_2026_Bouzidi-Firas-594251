# KinderCare Connect

Application Vue.js + Spring Boot pour le cours Webtechnologien. Le frontend garde les pages Login, Parent et Admin ainsi que le design initial. Le backend reste volontairement petit et sauvegarde seulement les données utiles dans PostgreSQL.

## Structure

```text
backend/src/main/java/de/htw_berlin/kindercare/
├── config/                    # CORS et données de démonstration
├── child/                     # Child: Entity, Repository, Service, Controller
├── medication/                # Medication: Entity, Repository, Service, Controller
├── staff/                     # Staff: Entity, Repository, Service, Controller
└── KinderCareApplication.java # démarrage Spring Boot

frontend/src/
├── assets/                    # image et icônes
├── components/                # composants réutilisables du design
├── views/                     # Login, ParentDashboard, AdminDashboard
├── services/                  # API backend et API externes
├── state/                     # état Vue léger
└── __tests__/                 # tests Vitest
```

Chaque domaine backend suit le même chemin clair :

`Controller → Service → Repository → PostgreSQL`

## Données sauvegardées

- `Child` : nom, allergies
- `Medication` : nom, enfant, dosage
- `Staff` : nom, rôle

Routes REST :

- `GET` / `POST` `/api/children`
- `GET` / `POST` `/api/medications`
- `GET` / `POST` `/api/staff`

## API externes

- Open-Meteo : météo
- openFDA : informations publiques sur les médicaments
- Nager.Date : jours fériés allemands

## Tester localement

### 1. Démarrer PostgreSQL

```powershell
docker compose up -d db
```

### 2. Démarrer le backend

Depuis la racine du projet :

```powershell
.\gradlew :backend:bootRun
```

Le backend est disponible sur `http://localhost:8080`.

### 3. Vérifier la sauvegarde backend

Créer un enfant :

```powershell
$child = @{ name = 'Test Child'; allergies = 'None' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/children -ContentType 'application/json' -Body $child
```

Lire les enfants :

```powershell
Invoke-RestMethod http://localhost:8080/api/children
```

Vérifier directement PostgreSQL :

```powershell
docker compose exec db psql -U kindercare -d kindercare -c "SELECT * FROM children;"
```

### 4. Démarrer et vérifier le frontend

```powershell
cd frontend
npm install
npm run dev
```

Ouvrir :

- `http://localhost:5173/#/parent` : ajouter un enfant puis actualiser la page.
- `http://localhost:5173/#/admin` : ajouter un médicament via le calendrier puis actualiser la page.

Si les données restent visibles après actualisation et dans la commande PostgreSQL, elles sont bien enregistrées.

## Tests automatisés

```powershell
.\gradlew :backend:test
cd frontend
npm test
```

GitHub Actions lance ces deux commandes à chaque `push` et `pull request`.

## Render

`render.yaml` contient un backend Docker et un frontend statique. Il utilise la base PostgreSQL Render existante. Configurer sur Render :

- `DATABASE_URL` au format JDBC : `jdbc:postgresql://HOST:5432/kindercare`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `VITE_API_BASE_URL` : URL publique du backend, sans `/api`

Le backend active automatiquement le profil `prod` sur Render et le profil `local` en développement. Les vrais identifiants ne sont jamais ajoutés au dépôt.
