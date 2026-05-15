CREATE TABLE IF NOT EXISTS children (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255),
    date_of_birth DATE,
    allergies TEXT,
    chronic_diseases TEXT,
    special_needs TEXT,
    emergency_contact VARCHAR(255)
    );