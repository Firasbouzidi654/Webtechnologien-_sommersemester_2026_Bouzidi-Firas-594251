# Render Deployment Guide for KinderCareConnect

## Overview
This guide explains how to deploy the KinderCareConnect Spring Boot backend with PostgreSQL on Render.

## Prerequisites
- GitHub repository with the project pushed
- Render account (https://render.com)
- PostgreSQL database on Render

## Step 1: Create PostgreSQL Database on Render

1. Go to https://render.com/dashboard
2. Click "New +" and select "PostgreSQL"
3. Choose a name: `kindercare-connect-db`
4. Region: Choose closest to your location
5. PostgreSQL Version: 15 (or latest)
6. Click "Create Database"
7. Copy the connection details:
   - Database: `render_db_xxxxx`
   - User: `demo_user`
   - Password: (provided)
   - Host: `dpg-xxxxx.frankfurt-postgres.render.com`
   - Internal Database URL: postgresql://...
   - External Database URL: postgresql://...

## Step 2: Create Web Service on Render

1. Go to https://render.com/dashboard
2. Click "New +" and select "Web Service"
3. Connect your GitHub repository:
   - Select repository: Your KinderCareConnect repo
   - Branch: main (or your branch)
   - Auto-deploy: Yes (recommended)

## Step 3: Configure Build and Start Commands

### Build Command:
```bash
cd backend && ./gradlew build
```

### Start Command:
```bash
cd backend && java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

## Step 4: Set Environment Variables on Render

Navigate to **Environment** tab and add:

```
# Database Connection (from PostgreSQL database created above)
DATABASE_URL=postgresql://demo_user:rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh@dpg-d80eo77lk1mc73da4qcg-a.frankfurt-postgres.render.com/render_db_9td1
DATABASE_USER=demo_user
DATABASE_PASSWORD=rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh

# Application Configuration
SPRING_PROFILES_ACTIVE=production
PORT=10000

# Flyway
FLYWAY_ENABLED=true

# JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
```

## Step 5: Instance Settings

- **Instance Type**: Free (for development/testing)
- **Auto-deploy**: Enabled
- **Health Check Path**: `/api/children` (optional)

## Step 6: Deploy

1. Click "Create Web Service"
2. Wait for build to complete (2-5 minutes)
3. Check logs for any errors
4. Once deployed, your service will have a URL like: `https://kindercare-connect-xxxxx.onrender.com`

## Testing the Deployment

### Test API Endpoint:
```bash
curl https://kindercare-connect-xxxxx.onrender.com/api/children
```

### Expected Response (successful):
```json
[
  {
    "id": 1,
    "name": "Anna Schmidt",
    "dateOfBirth": "2020-03-15",
    "allergies": "Peanuts, Milk",
    ...
  }
]
```

## Troubleshooting

### Build Fails
- Check logs in Render dashboard
- Verify Java version is 21
- Ensure all dependencies in `build.gradle` are correct
- Check for compilation errors

### Database Connection Error
- Verify DATABASE_URL is correct
- Check that Render PostgreSQL database is running (not paused)
- Verify credentials in environment variables
- Test connection with: `psql connection_string`

### Application Crashes After Startup
- Check logs for specific error messages
- Verify Flyway migrations run successfully
- Ensure Hibernate DDL setting is `validate` (not `create` or `create-drop`)

### Slow Performance
- Check database query logs
- Verify HikariCP pool settings in application-production.properties
- Monitor Render's resource usage

## Local Development with PostgreSQL

To test locally before deploying:

1. Install PostgreSQL locally
2. Create database: `createdb kindercare`
3. Run backend with PostgreSQL profile:
   ```bash
   cd backend
   ./gradlew bootRun --args='--spring.profiles.active=postgresql'
   ```

4. Frontend development (separate terminal):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Database Migrations with Flyway

Flyway automatically migrates the database when:
1. Application starts with `FLYWAY_ENABLED=true`
2. Migration files are in `backend/src/main/resources/db/migration/`
3. Files are named: `V1__Initial_Schema.sql`, `V2__...add_column.sql`, etc.

To manually migrate:
```bash
./gradlew flywayMigrate
```

## Important Notes

⚠️ **Security**
- Never commit `.env` files with credentials
- Use Render's environment variables for sensitive data
- Keep PostgreSQL connection info in Render secrets only

⚠️ **Performance**
- Free tier Render instances have resource limits
- Connection pool is set to 5 for production (limited resources)
- Database queries should be optimized
- Consider paid plan for production use

⚠️ **Monitoring**
- Check Render dashboard logs regularly
- Set up monitoring/alerts if available
- Monitor database usage on Render PostgreSQL

## Useful Commands

### Check application logs:
- Render dashboard → Logs tab
- Or use: `render logs`

### Connect to production database:
```bash
psql postgresql://demo_user:PASSWORD@dpg-xxxxx.frankfurt-postgres.render.com/render_db_9td1
```

### View database tables:
```sql
\dt
SELECT * FROM children;
```

## Frontend Integration

Update frontend API base URL for production:

**In `frontend/src/services/kindercareApi.js`**:
```javascript
const API_BASE = process.env.VUE_APP_API_URL || 'https://kindercare-connect-xxxxx.onrender.com/api';
```

Update `.env.production`:
```
VUE_APP_API_URL=https://kindercare-connect-xxxxx.onrender.com/api
```

## Support & Additional Resources

- Render Documentation: https://render.com/docs
- Spring Boot on Render: https://render.com/docs/deploy-spring-boot
- PostgreSQL on Render: https://render.com/docs/databases
- Flyway Documentation: https://flywaydb.org/documentation/

