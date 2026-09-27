CREATE TABLE purchase_orders (
 id UUID PRIMARY KEY,
 customer VARCHAR(120) NOT NULL,
 product VARCHAR(80) NOT NULL,
 quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 1000),
 unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
 status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','CANCELLED')),
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_orders_created ON purchase_orders(created_at DESC, id DESC);
