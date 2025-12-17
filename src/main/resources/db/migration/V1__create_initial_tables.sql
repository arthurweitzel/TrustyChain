CREATE TABLE actors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    public_key TEXT NOT NULL
);

CREATE TABLE product_chain (
    id UUID PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    metadata VARCHAR(255),
    previous_hash VARCHAR(255),
    current_hash VARCHAR(255) NOT NULL,
    signature TEXT NOT NULL,
    public_key_snapshot TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
