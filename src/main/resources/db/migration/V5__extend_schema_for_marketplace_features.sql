-- Extend schema for vehicle categories, localization, recently viewed tracking, and SEO metadata.

ALTER TABLE cars
ADD COLUMN IF NOT EXISTS category VARCHAR(32);

UPDATE cars
SET category = CASE
    WHEN body_type = 'PICKUP' OR body_type = 'VAN' THEN 'COMMERCIAL_VEHICLE'
    WHEN "condition" = 'ACCIDENTADO' THEN 'DAMAGED'
    ELSE 'PASSENGER_CAR'
END
WHERE category IS NULL;

ALTER TABLE cars
ALTER COLUMN category SET NOT NULL;

ALTER TABLE car_images
ADD COLUMN IF NOT EXISTS image_order INTEGER;

WITH ordered_images AS (
    SELECT
        car_id,
        image_url,
        ROW_NUMBER() OVER (PARTITION BY car_id ORDER BY image_url) - 1 AS generated_order
    FROM car_images
)
UPDATE car_images ci
SET image_order = oi.generated_order
FROM ordered_images oi
WHERE ci.car_id = oi.car_id
  AND ci.image_url = oi.image_url
  AND ci.image_order IS NULL;

CREATE TABLE IF NOT EXISTS localized_content (
    id BIGSERIAL PRIMARY KEY,
    content_key VARCHAR(150) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_localized_content_key_locale UNIQUE (content_key, locale)
);

CREATE TABLE IF NOT EXISTS view_history (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(128) NOT NULL,
    car_id BIGINT NOT NULL,
    user_id BIGINT,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    CONSTRAINT fk_view_history_car FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT fk_view_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS seo_metadata (
    id BIGSERIAL PRIMARY KEY,
    page_key VARCHAR(150) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),
    og_title VARCHAR(255),
    og_description TEXT,
    og_image VARCHAR(500),
    structured_data TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_seo_metadata_page_locale UNIQUE (page_key, locale)
);

CREATE INDEX IF NOT EXISTS idx_car_condition ON cars("condition");
CREATE INDEX IF NOT EXISTS idx_car_category ON cars(category);
CREATE INDEX IF NOT EXISTS idx_view_session ON view_history(session_id);
CREATE INDEX IF NOT EXISTS idx_view_timestamp ON view_history(viewed_at);
