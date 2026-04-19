# Guide De Lancement

Ce fichier explique comment lancer le projet et tester ce qui fonctionne deja.

## 1. Ouvrir le bon dossier

Dans PowerShell, va d'abord a la racine du projet :

```powershell
cd "C:\Users\Firas\Desktop\Learnin_Python\WebTech\Webtechnologien-_sommersemester_2026_Bouzidi-Firas-594251"
```

## 2. Verifier que Gradle est bien la

```powershell
dir .\gradlew.bat
```

Si le fichier apparait, tu es dans le bon dossier.

## 3. Lancer le backend Spring Boot

```powershell
.\gradlew.bat :backend:bootRun
```

Ensuite ouvre dans le navigateur :

```text
http://localhost:8080/
```

Tu dois voir la page web du projet.

## 4. Tester la route GET du backend

Dans le navigateur :

```text
http://localhost:8080/api/children
```

Ou dans PowerShell :

```powershell
Invoke-RestMethod http://localhost:8080/api/children
```

Tu dois recevoir une liste JSON d'enfants d'exemple.

## 5. Lancer les tests backend

```powershell
.\gradlew.bat :backend:test
```

## 6. Construire le frontend

Va dans le dossier frontend :

```powershell
cd "C:\Users\Firas\Desktop\Learnin_Python\WebTech\Webtechnologien-_sommersemester_2026_Bouzidi-Firas-594251\frontend"
```

Puis lance :

```powershell
npm install
npm run build
```

## 7. Revenir a la racine du projet

```powershell
cd ..
```

## 8. Ce qu'il faut verifier

- La page `http://localhost:8080/` s'ouvre
- La route `http://localhost:8080/api/children` retourne du JSON
- Le dark mode fonctionne
- Le changement de langue fonctionne
- Le formulaire s'affiche correctement

## 9. Probleme courant

Si PowerShell dit que `.\gradlew.bat` n'existe pas, c'est presque toujours parce que tu n'es pas dans le bon dossier.

Reviens a la racine du projet avec :

```powershell
cd "C:\Users\Firas\Desktop\Learnin_Python\WebTech\Webtechnologien-_sommersemester_2026_Bouzidi-Firas-594251"
```
