-- Add persistence support for expandable advanced inventory filters.

ALTER TABLE dealers
ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

ALTER TABLE cars
ADD COLUMN IF NOT EXISTS color_code VARCHAR(32),
ADD COLUMN IF NOT EXISTS refined_fuel_type BOOLEAN,
ADD COLUMN IF NOT EXISTS origin VARCHAR(64),
ADD COLUMN IF NOT EXISTS registration_available BOOLEAN,
ADD COLUMN IF NOT EXISTS awaiting_verification BOOLEAN,
ADD COLUMN IF NOT EXISTS full_instruction_booklet BOOLEAN,
ADD COLUMN IF NOT EXISTS all_keys_available BOOLEAN,
ADD COLUMN IF NOT EXISTS engine_damage BOOLEAN,
ADD COLUMN IF NOT EXISTS lower_damage BOOLEAN,
ADD COLUMN IF NOT EXISTS drivable BOOLEAN,
ADD COLUMN IF NOT EXISTS movable BOOLEAN,
ADD COLUMN IF NOT EXISTS engine_runs BOOLEAN,
ADD COLUMN IF NOT EXISTS airbags_intact BOOLEAN;

UPDATE dealers
SET latitude = 40.4168,
    longitude = -3.7038
WHERE city = 'Madrid' AND latitude IS NULL AND longitude IS NULL;

UPDATE dealers
SET latitude = 41.3874,
    longitude = 2.1686
WHERE city = 'Barcelona' AND latitude IS NULL AND longitude IS NULL;

UPDATE dealers
SET latitude = 39.4699,
    longitude = -0.3763
WHERE city = 'Valencia' AND latitude IS NULL AND longitude IS NULL;

CREATE INDEX IF NOT EXISTS idx_cars_color_code ON cars(color_code);
CREATE INDEX IF NOT EXISTS idx_cars_origin ON cars(origin);
CREATE INDEX IF NOT EXISTS idx_dealers_coordinates ON dealers(latitude, longitude);
