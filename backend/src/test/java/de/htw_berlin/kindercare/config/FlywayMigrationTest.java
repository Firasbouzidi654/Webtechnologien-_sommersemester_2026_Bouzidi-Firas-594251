package de.htw_berlin.kindercare.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationTest {
    @Test
    void repairMigrationRemovesLegacyMedicationRequirementAndUnusedTables() throws Exception {
        String url = "jdbc:h2:mem:flyway-repair;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE children (id BIGINT PRIMARY KEY, name VARCHAR(255), allergies VARCHAR(255))");
            statement.execute("CREATE TABLE medications (id BIGINT PRIMARY KEY, medication_id VARCHAR(50) NOT NULL, child_id BIGINT, name VARCHAR(255), dosage VARCHAR(255), scheduled_time VARCHAR(10), scheduled_date DATE, today_status VARCHAR(50))");
            statement.execute("INSERT INTO medications (id, medication_id, child_id, name, dosage, scheduled_time, scheduled_date, today_status) VALUES (1, 'legacy-1', 1, 'Vitamin D', '5 drops', '08:00', DATE '2026-06-23', 'PENDING')");
            statement.execute("CREATE TABLE parent_notes (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE emergency_contacts (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE medication_logs (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE staff (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE app_users (id BIGINT PRIMARY KEY, email VARCHAR(255), password_hash VARCHAR(255), role VARCHAR(50))");
        }

        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            var metadata = connection.getMetaData();
            assertTrue(columnExists(metadata, "MEDICATIONS", "FREQUENCY"));
            assertTrue(columnExists(metadata, "MEDICATIONS", "INTERVAL_DAYS"));
            assertTrue(columnExists(metadata, "MEDICATIONS", "START_DATE"));
            assertFalse(columnExists(metadata, "MEDICATIONS", "MEDICATION_ID"));
            assertFalse(tableExists(metadata, "PARENT_NOTES"));
            assertFalse(tableExists(metadata, "EMERGENCY_CONTACTS"));
            assertFalse(tableExists(metadata, "MEDICATION_LOGS"));
            assertFalse(tableExists(metadata, "STAFF"));
            assertFalse(tableExists(metadata, "APP_USERS"));
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT start_date FROM medications WHERE id = 1")) {
                assertTrue(result.next());
                assertTrue("2026-06-23".equals(result.getString(1)));
            }
        }
    }

    private boolean tableExists(java.sql.DatabaseMetaData metadata, String tableName) throws Exception {
        try (var result = metadata.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return result.next();
        }
    }

    private boolean columnExists(java.sql.DatabaseMetaData metadata, String tableName, String columnName) throws Exception {
        try (var result = metadata.getColumns(null, null, tableName, columnName)) {
            return result.next();
        }
    }
}
