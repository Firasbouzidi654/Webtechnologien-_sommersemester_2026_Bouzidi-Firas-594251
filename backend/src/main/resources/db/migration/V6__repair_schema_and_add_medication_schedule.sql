
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS children (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    allergies TEXT
);

CREATE TABLE IF NOT EXISTS medications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    child_name VARCHAR(255),
    child_id BIGINT,
    dosage VARCHAR(255),
    medication_time VARCHAR(5),
    status VARCHAR(20),
    frequency VARCHAR(20),
    interval_days INTEGER,
    start_date VARCHAR(10)
);

-- Preserve usable legacy records while converting the old medication shape.
ALTER TABLE medications ADD COLUMN IF NOT EXISTS child_name VARCHAR(255);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS child_id BIGINT;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS dosage VARCHAR(255);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS medication_time VARCHAR(5);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS status VARCHAR(20);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS frequency VARCHAR(20);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS interval_days INTEGER;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS start_date VARCHAR(10);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS scheduled_time VARCHAR(10);
ALTER TABLE medications ADD COLUMN IF NOT EXISTS scheduled_date DATE;
ALTER TABLE medications ADD COLUMN IF NOT EXISTS today_status VARCHAR(50);

UPDATE medications
SET child_name = COALESCE(child_name, (SELECT name FROM children WHERE children.id = medications.child_id), 'Unknown child');
UPDATE medications SET medication_time = COALESCE(medication_time, scheduled_time, '12:00');
UPDATE medications SET status = COALESCE(status, today_status, 'PENDING');
UPDATE medications SET frequency = COALESCE(frequency, 'DAILY');
UPDATE medications SET start_date = COALESCE(start_date, CAST(scheduled_date AS VARCHAR));

-- medication_id was mandatory in the retired model but no longer has a producer.
ALTER TABLE medications DROP COLUMN IF EXISTS medication_id;
ALTER TABLE medications DROP COLUMN IF EXISTS active_ingredient;
ALTER TABLE medications DROP COLUMN IF EXISTS instructions;
ALTER TABLE medications DROP COLUMN IF EXISTS prescription_uploaded;
ALTER TABLE medications DROP COLUMN IF EXISTS today_status;
ALTER TABLE medications DROP COLUMN IF EXISTS scheduled_time;
ALTER TABLE medications DROP COLUMN IF EXISTS scheduled_date;
ALTER TABLE medications DROP COLUMN IF EXISTS qr_payload;
ALTER TABLE medications DROP COLUMN IF EXISTS day_part;

-- These columns belonged to the removed child-profile prototype and have no
-- current frontend or backend consumer.
ALTER TABLE children DROP COLUMN IF EXISTS date_of_birth;
ALTER TABLE children DROP COLUMN IF EXISTS chronic_diseases;
ALTER TABLE children DROP COLUMN IF EXISTS special_needs;
ALTER TABLE children DROP COLUMN IF EXISTS emergency_contact;
ALTER TABLE children DROP COLUMN IF EXISTS group_name;
ALTER TABLE children DROP COLUMN IF EXISTS parent_name;
ALTER TABLE children DROP COLUMN IF EXISTS parent_email;
ALTER TABLE children DROP COLUMN IF EXISTS health_notes;
ALTER TABLE children DROP COLUMN IF EXISTS photo_url;

-- Migrate accounts from the superseded app_users table before removing it.
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    password_hash VARCHAR(255),
    role VARCHAR(50)
);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS role VARCHAR(50);
INSERT INTO users (email, password, role)
SELECT legacy.email, legacy.password_hash, COALESCE(legacy.role, 'PARENT')
FROM app_users legacy
WHERE legacy.email IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users existing_user WHERE existing_user.email = legacy.email);

DROP TABLE IF EXISTS emergency_contacts;
DROP TABLE IF EXISTS parent_notes;
DROP TABLE IF EXISTS medication_logs;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS app_users;

CREATE INDEX IF NOT EXISTS idx_medications_child_id ON medications(child_id);
