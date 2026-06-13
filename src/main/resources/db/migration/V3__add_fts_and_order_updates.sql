-- Convert enum column types to VARCHAR(30)
ALTER TABLE orders ALTER COLUMN status TYPE VARCHAR(30) USING status::text;
ALTER TABLE order_status_history ALTER COLUMN old_status TYPE VARCHAR(30) USING old_status::text;
ALTER TABLE order_status_history ALTER COLUMN new_status TYPE VARCHAR(30) USING new_status::text;

-- Update to uppercase values mapping
UPDATE orders SET status = 'PENDING' WHERE status = 'pending';
UPDATE orders SET status = 'CONFIRMED' WHERE status = 'confirmed';
UPDATE orders SET status = 'PACKING' WHERE status = 'processing';
UPDATE orders SET status = 'SHIPPING' WHERE status = 'shipping';
UPDATE orders SET status = 'DELIVERED' WHERE status = 'delivered';
UPDATE orders SET status = 'CANCELLED' WHERE status = 'cancelled';
UPDATE orders SET status = 'RETURNED' WHERE status = 'refunded';

UPDATE order_status_history SET old_status = 'PENDING' WHERE old_status = 'pending';
UPDATE order_status_history SET old_status = 'CONFIRMED' WHERE old_status = 'confirmed';
UPDATE order_status_history SET old_status = 'PACKING' WHERE old_status = 'processing';
UPDATE order_status_history SET old_status = 'SHIPPING' WHERE old_status = 'shipping';
UPDATE order_status_history SET old_status = 'DELIVERED' WHERE old_status = 'delivered';
UPDATE order_status_history SET old_status = 'CANCELLED' WHERE old_status = 'cancelled';
UPDATE order_status_history SET old_status = 'RETURNED' WHERE old_status = 'refunded';

UPDATE order_status_history SET new_status = 'PENDING' WHERE new_status = 'pending';
UPDATE order_status_history SET new_status = 'CONFIRMED' WHERE new_status = 'confirmed';
UPDATE order_status_history SET new_status = 'PACKING' WHERE new_status = 'processing';
UPDATE order_status_history SET new_status = 'SHIPPING' WHERE new_status = 'shipping';
UPDATE order_status_history SET new_status = 'DELIVERED' WHERE new_status = 'delivered';
UPDATE order_status_history SET new_status = 'CANCELLED' WHERE new_status = 'cancelled';
UPDATE order_status_history SET new_status = 'RETURNED' WHERE new_status = 'refunded';

ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'PENDING';

-- Link address to orders
ALTER TABLE orders ADD COLUMN address_id BIGINT REFERENCES addresses(id) ON DELETE SET NULL;

-- Create shipment_status_history table
CREATE TABLE shipment_status_history (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shipment_id   BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status        VARCHAR(30) NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_shipment_status_history_shipment ON shipment_status_history(shipment_id);

-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(100) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Add FTS support to products
ALTER TABLE products ADD COLUMN tsv tsvector GENERATED ALWAYS AS (
  to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, ''))
) STORED;

CREATE INDEX idx_products_tsv ON products USING gin(tsv);

-- Create fts_match function for cleaner criteria queries
CREATE OR REPLACE FUNCTION fts_match(tsv tsvector, query_str text)
RETURNS boolean AS $$
BEGIN
  RETURN tsv @@ plainto_tsquery('english', query_str);
END;
$$ LANGUAGE plpgsql IMMUTABLE;
