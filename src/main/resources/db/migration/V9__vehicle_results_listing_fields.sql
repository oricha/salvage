-- Add fields required by the enriched vehicle results listing.

ALTER TABLE cars
ADD COLUMN IF NOT EXISTS variant VARCHAR(120),
ADD COLUMN IF NOT EXISTS export_price DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS power_hp INTEGER;

UPDATE cars
SET variant = CASE
    WHEN make = 'Mercedes-Benz' AND model = 'C 300' THEN 'Avantgarde'
    WHEN make = 'BMW' AND model = '320d' THEN 'M Sport'
    WHEN make = 'Audi' AND model = 'A4' THEN 'Advanced'
    WHEN make = 'Tesla' AND model = 'Model 3' THEN 'Long Range'
    WHEN make = 'Porsche' AND model = '911 Carrera' THEN 'Carrera'
    WHEN make = 'Ferrari' AND model = '488 GTB' THEN 'F1 DCT'
    WHEN make = 'Lamborghini' AND model = 'Huracán' THEN 'EVO'
    WHEN make = 'Volkswagen' AND model = 'Golf' THEN 'Life'
    WHEN make = 'Seat' AND model = 'León' THEN 'FR'
    WHEN make = 'Toyota' AND model = 'Corolla' THEN 'Style'
    WHEN make = 'Nissan' AND model = 'Qashqai' THEN 'N-Connecta'
    WHEN make = 'Renault' AND model = 'Clio' THEN 'Zen'
    ELSE variant
END
WHERE variant IS NULL;

UPDATE cars
SET power_hp = CASE
    WHEN make = 'Mercedes-Benz' AND model = 'C 300' THEN 258
    WHEN make = 'BMW' AND model = '320d' THEN 190
    WHEN make = 'Audi' AND model = 'A4' THEN 204
    WHEN make = 'Tesla' AND model = 'Model 3' THEN 351
    WHEN make = 'Porsche' AND model = '911 Carrera' THEN 385
    WHEN make = 'Ferrari' AND model = '488 GTB' THEN 670
    WHEN make = 'Lamborghini' AND model = 'Huracán' THEN 640
    WHEN make = 'Volkswagen' AND model = 'Golf' THEN 150
    WHEN make = 'Seat' AND model = 'León' THEN 150
    WHEN make = 'Toyota' AND model = 'Corolla' THEN 140
    WHEN make = 'Nissan' AND model = 'Qashqai' THEN 158
    WHEN make = 'Renault' AND model = 'Clio' THEN 120
    ELSE power_hp
END
WHERE power_hp IS NULL;

UPDATE cars
SET export_price = ROUND(price * 0.92, 2)
WHERE export_price IS NULL
  AND category IN ('DAMAGED', 'SALVAGE', 'COMMERCIAL_VEHICLE');
