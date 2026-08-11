DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM users;

ALTER TABLE order_items ALTER COLUMN id RESTART WITH 1;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (id, username, email, password, amount, restriction, is_admin) VALUES
    (1, 'alice_reads', 'alice@example.com', 'password123', 100.00, FALSE, FALSE),
    (2, 'bob_pages', 'bob@example.com', 'password123', 100.00, FALSE, FALSE),
    (3, 'restricted', 'restricted@example.com', 'password123', 100.00, TRUE, FALSE);

INSERT INTO orders (id, user_id, created_at) VALUES
    (1, 1, TIMESTAMP '2024-01-15 10:00:00'),
    (2, 2, TIMESTAMP '2024-02-10 12:00:00');

INSERT INTO order_items (id, order_id, book_id) VALUES
    (1, 1, 1),
    (2, 2, 2);

ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 3;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 3;
