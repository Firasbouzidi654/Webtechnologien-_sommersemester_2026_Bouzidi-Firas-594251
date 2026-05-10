# PostgreSQL Configuration Guide

## Overview
This project is fully configured to work with PostgreSQL databases in both local development and production environments on Render.

## Configuration Files

### 1. `application.properties` (Default - H2 for quick testing)
- Location: `backend/src/main/resources/application.properties`
- Uses H2 in-memory database by default
- Good for: Quick testing, CI/CD pipelines, development without database setup
- Database: In-memory H2

### 2. `application-postgresql.properties` (Local PostgreSQL)
- Location: `backend/src/main/resources/application-postgresql.properties`
- Uses local PostgreSQL instance
- Good for: Full feature development, testing database persistence
- Requirements: PostgreSQL installed locally

### 3. `application-production.properties` (Render PostgreSQL)
- Location: `backend/src/main/resources/application-production.properties`
- Uses environment variables for sensitive data
- Good for: Production deployment on Render
- Requirements: Environment variables set in Render dashboard

## How to Use Different Profiles

### Run with H2 (Default)
```bash
cd backend
./gradlew bootRun
```

### Run with Local PostgreSQL
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=postgresql'
```

### Run with Production Profile (simulating Render)
```bash
cd backend
export spring_profiles_active=production
export DATABASE_URL=postgresql://user:password@host:5432/database
./gradlew bootRun
```

## Setting Up Local PostgreSQL

### Windows
1. Download PostgreSQL from https://www.postgresql.org/download/windows/
2. Install with default settings (remember the password for `postgres` user)
3. Open psql and create database:
   ```sql
   CREATE DATABASE kindercare;
   CREATE USER kindercare_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE kindercare TO kindercare_user;
   ```

4. Update `application-postgresql.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/kindercare
   spring.datasource.username=kindercare_user
   spring.datasource.password=your_password
   ```

### macOS
```bash
# Using Homebrew
brew install postgresql
brew services start postgresql

# Create database
createdb kindercare
createuser kindercare_user
psql kindercare -c "ALTER USER kindercare_user WITH PASSWORD 'your_password'"
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get install postgresql postgresql-contrib
sudo -u postgres createdb kindercare
sudo -u postgres createuser kindercare_user
sudo -u postgres psql -c "ALTER USER kindercare_user WITH PASSWORD 'your_password'"
```

## Database Migrations with Flyway

Migrations are stored in: `backend/src/main/resources/db/migration/`

### Current Migrations:
- `V1__Initial_Schema.sql` - Creates all tables and indexes

### Adding New Migrations
1. Create new file: `V2__Description.sql`
2. Write SQL migration
3. Flyway automatically runs it on next startup (if `FLYWAY_ENABLED=true`)

### Example new migration:
```sql
-- V2__Add_column_to_children.sql
ALTER TABLE children ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

## Environment Variables for Production

### Required Variables on Render:
```
DATABASE_URL=postgresql://user:password@host:5432/database
DATABASE_USER=demo_user
DATABASE_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=production
PORT=10000
FLYWAY_ENABLED=true
```

### Example (with your actual Render database):
```
DATABASE_URL=postgresql://demo_user:rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh@dpg-d80eo77lk1mc73da4qcg-a.frankfurt-postgres.render.com/render_db_9td1
DATABASE_USER=demo_user
DATABASE_PASSWORD=rFM6UgUtpNItQTXfSWuyWktS1hkPFOLh
SPRING_PROFILES_ACTIVE=production
PORT=10000
FLYWAY_ENABLED=true
```

## Database Connection Pool Settings

### For Local Development:
- Max Pool Size: 10
- Min Idle: 2
- Connection Timeout: 30s
- Idle Timeout: 10 minutes
- Max Lifetime: 30 minutes

### For Render Production:
- Max Pool Size: 5 (limited by free tier resources)
- Min Idle: 1
- Connection Timeout: 30s
- Idle Timeout: 5 minutes
- Max Lifetime: 15 minutes

## Troubleshooting

### Error: "package org.postgresql does not exist"
Solution: Add PostgreSQL driver to `build.gradle` (already done in this project)

### Error: "connection refused"
- Verify PostgreSQL is running: `pg_isready`
- Check connection string in properties file
- Verify username and password

### Error: "Flyway validation failed"
- Flyway detected schema changes not covered by migrations
- Solution: Update schema with new migration or disable Flyway validation
- In `application-production.properties`: `spring.jpa.hibernate.ddl-auto=validate`

### Error: "Maximum pool size exceeded"
- Reduce max pool size in configuration
- Check for connection leaks in application code
- Verify HikariCP settings match environment

### Database is locked (local development)
```sql
-- Kill existing connections
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'kindercare'
AND pid <> pg_backend_pid();

-- Drop and recreate database
DROP DATABASE kindercare;
CREATE DATABASE kindercare;
```

## Monitoring Database

### Connect to PostgreSQL locally:
```bash
psql -U kindercare_user -d kindercare
```

### Common queries:
```sql
-- List all tables
\dt

-- View table structure
\d+ children

-- Count records
SELECT COUNT(*) FROM children;

-- View data
SELECT * FROM children;

-- Check running queries
SELECT * FROM pg_stat_activity;
```

### Connect to Render PostgreSQL:
```bash
psql postgresql://demo_user:password@dpg-xxxxx.frankfurt-postgres.render.com/render_db_9td1
```

## Performance Optimization

### Indexes
- All tables have appropriate indexes (created in migration)
- Check performance with: `EXPLAIN ANALYZE SELECT ...`

### Connection Pooling
- HikariCP is configured for optimal performance
- Adjust pool size based on load testing

### Query Optimization
- Use JPA Repository methods efficiently
- Avoid N+1 queries with proper JOIN FETCH
- Monitor query logs for slow queries

## Backup Strategy

### Local Database
```bash
pg_dump -U kindercare_user -d kindercare > backup.sql
```

### Restore
```bash
psql -U kindercare_user -d kindercare < backup.sql
```

### Render PostgreSQL
- Render automatically backs up PostgreSQL databases
- Point-in-time recovery available in Render dashboard
- See Render documentation for backup retention policy

## Security Best Practices

1. ✅ Never commit credentials to git
2. ✅ Use environment variables for sensitive data
3. ✅ Use strong passwords (min 12 characters)
4. ✅ Enable SSL for database connections
5. ✅ Restrict database access by IP (if possible)
6. ✅ Regular security updates for PostgreSQL
7. ✅ Monitor database access logs

## Integration with Render

See `RENDER_DEPLOYMENT.md` for complete Render deployment guide.

## Support

- PostgreSQL Documentation: https://www.postgresql.org/docs/
- Render Database Documentation: https://render.com/docs/databases
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Flyway: https://flywaydb.org/documentation/

