-- V2: Add timestamp columns for signed timestamps
ALTER TABLE product_chain ADD COLUMN trusted_timestamp TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE product_chain ADD COLUMN timestamp_signature TEXT NOT NULL DEFAULT '';

-- Remove defaults after migration (they're only for existing rows)
ALTER TABLE product_chain ALTER COLUMN trusted_timestamp DROP DEFAULT;
ALTER TABLE product_chain ALTER COLUMN timestamp_signature DROP DEFAULT;
