CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TEXT AS $$
BEGIN
    RETURN 'ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(nextval('order_number_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

INSERT INTO customers (first_name, last_name, email, phone, customer_type) VALUES
('John', 'Doe', 'john.doe@example.com', '+1234567890', 'REGULAR'),
('Jane', 'Smith', 'jane.smith@example.com', '+1234567891', 'PREMIUM'),
('Bob', 'Johnson', 'bob.johnson@example.com', '+1234567892', 'VIP'),
('Alice', 'Williams', 'alice.williams@example.com', '+1234567893', 'REGULAR'),
('Charlie', 'Brown', 'charlie.brown@example.com', '+1234567894', 'PREMIUM')
ON CONFLICT (email) DO NOTHING;


INSERT INTO products (name, description, price, category, stock_quantity, sku, weight_kg, status) VALUES
('MacBook Pro 16"', 'High-performance laptop for professionals', 2499.99, 'Electronics', 50, 'ELE-MACBOOK16', 2.1, 'ACTIVE'),
('iPhone 15 Pro', 'Latest smartphone with advanced features', 999.99, 'Electronics', 100, 'ELE-IPHONE15', 0.2, 'ACTIVE'),
('Wireless Headphones', 'Premium noise-cancelling headphones', 299.99, 'Electronics', 75, 'ELE-HEADPHONES', 0.3, 'ACTIVE'),
('Office Chair', 'Ergonomic office chair for comfort', 399.99, 'Furniture', 25, 'FUR-CHAIR001', 15.5, 'ACTIVE'),
('Standing Desk', 'Adjustable height standing desk', 699.99, 'Furniture', 15, 'FUR-DESK001', 35.0, 'ACTIVE'),
('Coffee Maker', 'Professional espresso machine', 199.99, 'Appliances', 30, 'APP-COFFEE01', 8.2, 'ACTIVE'),
('Running Shoes', 'High-performance athletic shoes', 129.99, 'Sports', 200, 'SPO-SHOES01', 0.8, 'ACTIVE'),
('Yoga Mat', 'Premium eco-friendly yoga mat', 49.99, 'Sports', 150, 'SPO-YOGAMAT', 1.2, 'ACTIVE'),
('Gaming Mouse', 'High-precision gaming mouse', 79.99, 'Electronics', 80, 'ELE-MOUSE01', 0.1, 'ACTIVE'),
('Bluetooth Speaker', 'Portable waterproof speaker', 89.99, 'Electronics', 60, 'ELE-SPEAKER', 0.5, 'ACTIVE')
ON CONFLICT (sku) DO NOTHING;


INSERT INTO addresses (street, city, country, postal_code, address_type, customer_id)
SELECT
    '123 Main St', 'New York', 'USA', '10001', 'BILLING', c.id
FROM customers c WHERE c.email = 'john.doe@example.com'
UNION ALL
SELECT
    '456 Oak Ave', 'Los Angeles', 'USA', '90210', 'SHIPPING', c.id
FROM customers c WHERE c.email = 'jane.smith@example.com'
ON CONFLICT DO NOTHING;


DO $$
DECLARE
    customer_id_1 BIGINT;
    customer_id_2 BIGINT;
    product_id_1 BIGINT;
    product_id_2 BIGINT;
    order_id_1 BIGINT;
    order_id_2 BIGINT;
BEGIN
    SELECT id INTO customer_id_1 FROM customers WHERE email = 'john.doe@example.com';
    SELECT id INTO customer_id_2 FROM customers WHERE email = 'jane.smith@example.com';
    SELECT id INTO product_id_1 FROM products WHERE sku = 'ELE-MACBOOK16';
    SELECT id INTO product_id_2 FROM products WHERE sku = 'ELE-IPHONE15';

    IF customer_id_1 IS NOT NULL AND product_id_1 IS NOT NULL THEN
        INSERT INTO orders (order_number, customer_id, status, total_amount, shipping_cost, tax_amount)
        VALUES (generate_order_number(), customer_id_1, 'DELIVERED', 2699.98, 99.99, 100.00)
        RETURNING id INTO order_id_1;

        INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
        VALUES (order_id_1, product_id_1, 1, 2499.99, 2499.99);
    END IF;

    IF customer_id_2 IS NOT NULL AND product_id_2 IS NOT NULL THEN
        INSERT INTO orders (order_number, customer_id, status, total_amount, shipping_cost, tax_amount)
        VALUES (generate_order_number(), customer_id_2, 'PROCESSING', 1089.98, 49.99, 40.00)
        RETURNING id INTO order_id_2;

        INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
        VALUES (order_id_2, product_id_2, 1, 999.99, 999.99);
    END IF;
END $$;
