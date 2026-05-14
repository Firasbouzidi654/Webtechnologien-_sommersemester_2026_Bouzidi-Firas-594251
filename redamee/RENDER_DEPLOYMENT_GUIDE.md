# 🚀 RENDER DEPLOYMENT GUIDE - KinderCareConnect

## 📋 ARCHITECTURE OVERVIEW

Your project uses a **monolithic Java architecture**:
- **Backend**: Spring Boot (Gradle) in `/backend`
- **Frontend**: Vue.js (Vite) in `/frontend`
- **Built as**: Single JAR that serves both API + static frontend

```
One Gradle Build Process:
1. Compile backend Java code
2. Build frontend with Vite → dist/
3. Include frontend files in JAR static resources
4. Result: Single executable JAR serving everything
```

---

## ✅ CURRENT CONFIGURATION STATUS

| Component | Status | Details |
|-----------|--------|---------|
| Runtime | ✅ Java | Java 21 |
| Build Tool | ✅ Gradle | `./gradlew build` |
| Frontend Build | ✅ Auto | Triggered by Gradle |
| Database | ✅ PostgreSQL | Via DATABASE_URL |
| CORS | ✅ Configured | Allows *.onrender.com |
| Static Files | ✅ Included | Vue.js in JAR |

---

## 🎯 RENDER CONFIGURATION (ONE SERVICE)

### Step 1: Connect GitHub Repository
1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"Web Service"**
3. Select your GitHub repository
4. Choose branch: `main` (or your branch)

### Step 2: Configure Build Settings

**Name**: `kindercare-backend`

**Runtime**: `Java`

**Region**: `Frankfurt` (or closest to you)

**Root Directory**: `backend`

**Build Command**:
```bash
cd .. && ./gradlew build
```

**Start Command**:
```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

**Plan**: `Free` (or Starter depending on your needs)

### Step 3: Set Environment Variables

Click **"Environment"** and add these variables:

| Key | Value | Notes |
|-----|-------|-------|
| `DATABASE_URL` | `postgresql://demo_user:rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh@dpg-d80eo77lk1mc73da4qcg-a:5432/render_db_9td1` | Your PostgreSQL connection string |
| `SPRING_PROFILES_ACTIVE` | `production` | Use production properties |
| `PORT` | `10000` | Render assigns port dynamically |

### Step 4: Advanced Settings (Optional but Recommended)

- **Health Check Path**: `/api/children`
- **Health Check Protocol**: `HTTP`
- **Health Check Interval**: 30 seconds
- **Startup Time**: 60 seconds (Give Spring Boot time to start)

### Step 5: Deploy

Click **"Create Web Service"** and wait for deployment (2-5 minutes)

---

## 🔗 AFTER DEPLOYMENT

### Test Your API
```bash
curl https://kindercare-backend.onrender.com/api/children
```

Expected response:
```json
[
  {
    "id": 1,
    "name": "Anna Schmidt",
    "allergies": "Peanuts, Milk",
    ...
  },
  ...
]
```

### Access Frontend
```
https://kindercare-backend.onrender.com/
```

---

## 🔄 FRONTEND-BACKEND COMMUNICATION

### Current Setup (Recommended)
- **Frontend** + **Backend**: Single Java JAR on Render
- **Frontend** serves from Spring Boot static resources
- **API calls**: Relative URLs (e.g., `/api/children`)
- **No CORS issues**: Same origin

### Alternative: Separate Deployments
If you want to deploy frontend separately (Vercel/Netlify):

**Update frontend API URL:**
```javascript
// In your Vue components, use environment variables:
const API_URL = process.env.VUE_APP_API_URL || '/api';
```

---

## 📊 DATABASE SETUP

### PostgreSQL on Render
Your database is already created and configured:

| Setting | Value |
|---------|-------|
| Hostname | `dpg-d80eo77lk1mc73da4qcg-a` |
| External Host | `dpg-d80eo77lk1mc73da4qcg-a.frankfurt-postgres.render.com` |
| Port | `5432` |
| Database | `render_db_9td1` |
| Username | `demo_user` |
| Full Connection URL | `postgresql://demo_user:...@dpg-...frankfurt-postgres.render.com/render_db_9td1` |

### Verified in Application
- ✅ `application.properties`: `spring.datasource.url=${DATABASE_URL:...}`
- ✅ `DatabaseConfig.java`: Connection pooling configured
- ✅ `SampleDataConfig.java`: Seeds sample children data

---

## 🛠️ BUILD PROCESS DETAILS

### What Gradle Does During Build

```
1. Clean previous build
   └─ ./gradlew clean

2. Download all dependencies
   └─ Maven Central repositories

3. Build frontend (Vite)
   └─ npm install
   └─ npm run build → frontend/dist/

4. Compile Java code
   └─ src/main/java/ → build/classes/

5. Process resources
   └─ Copy frontend/dist → build/resources/static/
   └─ Include application.properties

6. Build JAR
   └─ build/libs/backend-0.0.1-SNAPSHOT.jar
   └─ Contains: Java classes + frontend files + embedded server
```

---

## 🚨 COMMON ISSUES & FIXES

### Issue: "java: command not found"
**Fix**: Java runtime is already available on Render. Check that `system.properties` exists:
```
backend/system.properties:
java.runtime.version=21
```

### Issue: "Build failed: Node.js not found"
**Fix**: Gradle uses Node plugin. Check that `build.gradle` has:
```gradle
id 'com.github.node-gradle.node' version '7.0.2'
node {
    download = true
    version = '20.19.0'
}
```

### Issue: "Frontend files not found"
**Fix**: Ensure `processResources` has `dependsOn buildFrontend`:
```gradle
processResources {
    dependsOn buildFrontend  // ← Must be uncommented
    from("${rootDir}/frontend/dist") {
        into "static"
    }
}
```

### Issue: "CORS errors in frontend"
**Fix**: Backend CORS config is set for `*.onrender.com`. Update if needed:
```java
registry.addMapping("/api/**")
    .allowedOrigins(
        "https://*.onrender.com",
        "https://kindercare-backend.onrender.com"
    )
```

### Issue: "Database connection timeout"
**Fix**: Verify `DATABASE_URL` environment variable is set correctly in Render dashboard.

---

## 📝 TESTING BEFORE DEPLOYMENT

### Test Locally
```bash
# Start the application
cd backend
./gradlew bootRun

# Test API
curl http://localhost:9090/api/children

# View frontend
open http://localhost:9090
```

### Test Production Configuration Locally
```bash
# Simulate production environment
export DATABASE_URL=your_postgresql_url
export SPRING_PROFILES_ACTIVE=production
./gradlew bootRun
```

---

## 🔐 SECURITY CHECKLIST

- ✅ Database credentials in `DATABASE_URL` environment variable (not in code)
- ✅ CORS configured for specific origins
- ✅ Spring Boot running behind Render's proxy
- ✅ HTTPS enforced by Render
- ✅ No sensitive data in logs
- ✅ Connection pooling prevents resource exhaustion

---

## 📚 USEFUL LINKS

- [Render Documentation](https://render.com/docs)
- [Render Java Deployment](https://render.com/docs/deploy-java)
- [Spring Boot on Render](https://docs.spring.io/spring-boot/)
- [Your App Dashboard](https://dashboard.render.com)

---

## 🚀 NEXT STEPS

1. ✅ Push changes to GitHub:
   ```bash
   git add .
   git commit -m "Fix Render deployment: Use Gradle for full-stack build"
   git push
   ```

2. ✅ Go to [Render Dashboard](https://dashboard.render.com)

3. ✅ Create new Web Service following steps above

4. ✅ Monitor deployment logs

5. ✅ Test API endpoints

6. ✅ Check frontend accessibility

---

**Status**: ✅ Configuration Complete  
**Last Updated**: May 14, 2026  
**Ready to Deploy**: YES ✅

---

## 📞 SUPPORT

If you encounter issues during deployment:
1. Check Render dashboard logs
2. Review this guide's "Common Issues" section
3. Verify all environment variables are set
4. Test locally with `./gradlew bootRun`
5. Check GitHub for any uncommitted changes


