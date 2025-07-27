CREATE INDEX IF NOT EXISTS idx_customers_email_partial ON customers(email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customers_type_created ON customers(customer_type, created_at);
CREATE INDEX IF NOT EXISTS idx_customer_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customer_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customer_created_at ON customers(created_at);

CREATE INDEX IF NOT EXISTS idx_products_category_status ON products(category, status);
CREATE INDEX IF NOT EXISTS idx_products_price_range ON products(price) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_products_active_stock ON products(stock_quantity) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

CREATE INDEX IF NOT EXISTS idx_orders_customer_status_date ON orders(customer_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_delivered_revenue ON orders(total_amount, delivered_at) WHERE status = 'DELIVERED';
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE INDEX IF NOT EXISTS idx_order_items_product_quantity ON order_items(product_id, quantity);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

CREATE INDEX IF NOT EXISTS idx_addresses_customer_id ON addresses(customer_id);
