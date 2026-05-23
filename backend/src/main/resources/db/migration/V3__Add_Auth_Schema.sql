CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'PARENT',
    phone VARCHAR(50),
    created_at VARCHAR(50)
);

-- Ensure columns exist if table was previously created with fewer fields
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS created_at VARCHAR(50);
