# Guide de lancement - KinderCare Connect

Ce guide explique comment lancer le prototype depuis le dossier actuel du projet.

## 1. Ouvrir le dossier du projet

Dans PowerShell :

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned"
```

Verifier que Gradle est disponible :

```powershell
dir .\gradlew.bat
```

## 2. Lancer le backend Spring Boot

Depuis la racine du projet :

```powershell
.\gradlew.bat :backend:bootRun
```

Le backend tourne sur :

```text
http://localhost:8080
```

Routes utiles a tester :

```text
http://localhost:8080/api/children
http://localhost:8080/api/children/1
http://localhost:8080/api/medications
http://localhost:8080/api/admin/tasks/today
http://localhost:8080/api/admin/stats/today
```

Tester une confirmation de medicament dans PowerShell :

```powershell
Invoke-RestMethod -Method Put http://localhost:8080/api/medications/MED-001/taken
```

## 3. Lancer le frontend Vue

Ouvre un deuxieme terminal PowerShell :

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned\frontend"
npm install
npm run dev
```

Le frontend tourne normalement sur :

```text
http://localhost:5173
```

Pages du prototype :

```text
http://localhost:5173/login
http://localhost:5173/parent
http://localhost:5173/admin
```

## 4. Que faire sur la page login

1. Ouvrir `http://localhost:5173/login`.
2. Laisser les valeurs de test ou ecrire n'importe quel email/mot de passe.
3. Choisir le role `Parent` pour aller vers le parent dashboard.
4. Choisir le role `Kindergarten staff / administrator` pour aller vers le admin dashboard.

## 5. Tester le prototype

Frontend :

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned\frontend"
npm run build
```

Backend :

```powershell
cd "C:\Users\Firas\Documents\UNI LSF\5 Semester\Webtechnologien\Cloned"
.\gradlew.bat :backend:test
```

## 6. Notes importantes

- PostgreSQL n'est pas connecte pour l'instant.
- Le login est un mock simple avec selection du role.
- Les donnees sont des exemples locaux.
- Chaque medicament a un identifiant unique non modifiable, par exemple `MED-001` et `MED-002`.
- Deux enfants peuvent utiliser le meme medicament, mais ils ont toujours deux IDs et deux QR payloads differents.

Pour plus de details sur la structure et les prochaines etapes, lire `KINDERCARE_PROTOTYPE_GUIDE.md`.
