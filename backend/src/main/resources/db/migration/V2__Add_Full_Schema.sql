-- Add missing columns to children table
ALTER TABLE children ADD COLUMN IF NOT EXISTS group_name VARCHAR(255);
ALTER TABLE children ADD COLUMN IF NOT EXISTS parent_name VARCHAR(255);
ALTER TABLE children ADD COLUMN IF NOT EXISTS parent_email VARCHAR(255);
ALTER TABLE children ADD COLUMN IF NOT EXISTS health_notes TEXT;

-- Create medications table
CREATE TABLE IF NOT EXISTS medications (
    id BIGSERIAL PRIMARY KEY,
    medication_id VARCHAR(50) UNIQUE NOT NULL,
    child_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    active_ingredient VARCHAR(255),
    dosage VARCHAR(255),
    instructions TEXT,
    prescription_uploaded BOOLEAN DEFAULT FALSE,
    today_status VARCHAR(50) DEFAULT 'Pending',
    scheduled_time VARCHAR(10) DEFAULT '12:00',
    scheduled_date DATE,
    qr_payload VARCHAR(500),
    frequency VARCHAR(50) DEFAULT 'Daily',
    day_part VARCHAR(50) DEFAULT 'Morning',
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

-- Create emergency contacts table
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGSERIAL PRIMARY KEY,
    child_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(255),
    priority INTEGER DEFAULT 1,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

-- Create parent notes table (one note per child, upsert pattern)
CREATE TABLE IF NOT EXISTS parent_notes (
    id BIGSERIAL PRIMARY KEY,
    child_id BIGINT NOT NULL,
    note TEXT,
    updated_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

-- Create medication logs table
CREATE TABLE IF NOT EXISTS medication_logs (
    id BIGSERIAL PRIMARY KEY,
    medication_id VARCHAR(50) NOT NULL,
    child_id BIGINT NOT NULL,
    status VARCHAR(50),
    admin_name VARCHAR(255),
    logged_at VARCHAR(50),
    note TEXT
);
