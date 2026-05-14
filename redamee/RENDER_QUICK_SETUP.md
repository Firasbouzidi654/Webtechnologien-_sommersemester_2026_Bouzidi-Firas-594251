# 📋 RENDER DASHBOARD CONFIGURATION - COPIER-COLLER

Use this checklist to configure your app on Render.com

## ✅ STEP 1: Connect Repository
- [ ] Create new Web Service
- [ ] Select GitHub repository: `your-username/Cloned`
- [ ] Select branch: `main`

---

## ✅ STEP 2: Settings

### Basic Configuration:
```
Name:                    kindercare-backend

Runtime:                 Java

Region:                  Frankfurt

Root Directory:          backend

Build Command:           ./mvnw -DskipTests clean package

Start Command:           java -jar target/backend-0.0.1-SNAPSHOT.jar

Plan:                    Free (or Starter for more power)
```

---

## ✅ STEP 3: Environment Variables

Add these variables in Render Dashboard under **Environment**:

```
DATABASE_URL = postgresql://demo_user:rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh@dpg-d80eo77lk1mc73da4qcg-a:5432/render_db_9td1

SPRING_PROFILES_ACTIVE = production

PORT = 10000
```

---

## ✅ STEP 4: Advanced (Optional but Good to Have)

**Health Check:**
- Path: `/api/children`
- Protocol: `HTTP`
- Interval: 30 seconds

**Memory:**
- Instance Size: Starter (0.5GB) or higher

---

## ✅ STEP 5: Deploy

Click **"Create Web Service"** and wait for it to finish building and deploying.

---

## ✅ TEST YOUR DEPLOYMENT

### Test API:
```bash
curl https://kindercare-backend.onrender.com/api/children
```

Expected 2xx response with JSON data ✅

### Test Frontend:
```
https://kindercare-backend.onrender.com/
```

Should show your Vue.js frontend ✅

---

## 🔄 IF BUILD FAILS

### Most Common Issues:

**1. Build timeout (try increasing timeout in Render settings)**
- Solution: Wait for build to complete (first build takes longer because Maven downloads

 npm/Node.js)

**2. Database connection error**
- Solution: Verify DATABASE_URL is set correctly

**3. Java version error**
- Solution: Confirm `backend/system.properties` contains `java.runtime.version=21`

**4. Frontend not included**
- Solution: Maven frontend-maven-plugin will auto-build Vite and include in JAR

---

## 🔑 YOUR ENVIRONMENT VARIABLES

Save these for reference:

```
DATABASE_URL:
postgresql://demo_user:rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh@dpg-d80eo77lk1mc73da4qcg-a:5432/render_db_9td1

SPRING_PROFILES_ACTIVE: production
```

---

## 📊 MONITORING

After deployment:
1. Go to **Render Dashboard** → Your Service
2. Click **"Logs"** to see real-time logs
3. Look for: "Started KinderCareConnectApplication"
4. Check health status indicator (should be green ✅)

---

**Ready? Let's Go!** 🚀


