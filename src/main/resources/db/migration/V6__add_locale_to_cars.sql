ALTER TABLE cars
ADD COLUMN IF NOT EXISTS locale VARCHAR(5);

UPDATE cars
SET locale = 'es'
WHERE locale IS NULL OR locale = '';

ALTER TABLE cars
ALTER COLUMN locale SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_car_locale ON cars(locale);
