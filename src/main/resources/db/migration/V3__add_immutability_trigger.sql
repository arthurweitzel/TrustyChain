-- V3: Add immutability trigger to prevent modification of product_chain records

-- Audit table for attempted violations
CREATE TABLE audit_immutability_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    attempted_at TIMESTAMP DEFAULT NOW(),
    old_data JSONB,
    attempted_by VARCHAR(255)
);

-- Trigger function to prevent modifications and log attempts
CREATE OR REPLACE FUNCTION prevent_chain_modification()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_immutability_violations (table_name, operation, old_data, attempted_by)
    VALUES (TG_TABLE_NAME, TG_OP, row_to_json(OLD), current_user);
    RAISE EXCEPTION 'Modification of product_chain records is not allowed. Attempt logged.';
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to product_chain table
CREATE TRIGGER enforce_immutability
BEFORE UPDATE OR DELETE ON product_chain
FOR EACH ROW EXECUTE FUNCTION prevent_chain_modification();

-- Index for audit log queries
CREATE INDEX idx_audit_violations_attempted_at ON audit_immutability_violations(attempted_at);
