INSERT INTO users (username, email, password, amount)
SELECT
    'bulk_user_' || gs,
    'bulk_user_' || gs || '@example.com',
    'hashed_pw',
    100.00
FROM generate_series(1, 100000) AS gs
ON CONFLICT (username) DO NOTHING;

INSERT INTO books (title, author, description, price)
SELECT 'Bulk Book ' || gs,
    'Author ' || lpad((gs % 10000)::text, 4, '0'),
    'Generated benchmark book ' || gs,
    ((gs % 20000) + 100)::numeric / 100
FROM generate_series(1, 2000000) AS gs;

INSERT INTO reviews (user_id, book_id, rating, comment)
SELECT
    u.id,
    b.id,
    ((b.id % 5) + 1)::int,
    'Generated benchmark review'
FROM (
    SELECT id, row_number() OVER () AS rn
    FROM users
    WHERE username LIKE 'bulk_user_%'
    ORDER BY id
) AS u
JOIN (
    SELECT id, row_number() OVER () AS rn
    FROM books
    WHERE title LIKE 'Bulk Book %'
    ORDER BY id
    LIMIT 2000000
) AS b ON ((b.rn - 1) % 100000) + 1 = u.rn;
