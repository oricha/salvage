-- Placeholder tables for future marketplace categories and contact interaction analytics.

CREATE TABLE IF NOT EXISTS used_parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    condition VARCHAR(50),
    vehicle_compatibility VARCHAR(255),
    dealer_id BIGINT NOT NULL,
    images TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_used_parts_dealer FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS occasion_vehicles (
    id BIGSERIAL PRIMARY KEY,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    vehicle_year INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    mileage INT,
    fuel_type VARCHAR(20),
    transmission VARCHAR(20),
    body_type VARCHAR(20),
    color VARCHAR(30),
    description TEXT,
    dealer_id BIGINT NOT NULL,
    images TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occasion_vehicles_dealer FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contact_interactions (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    user_id BIGINT,
    interaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    CONSTRAINT fk_contact_interactions_car FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_interactions_dealer FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_interactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_contact_interactions_dealer_timestamp ON contact_interactions(dealer_id, timestamp DESC);
