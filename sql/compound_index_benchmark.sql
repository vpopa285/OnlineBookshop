DROP INDEX IF EXISTS idx_books_author_price_title;
CREATE INDEX idx_books_author_price_title ON books(author, price, title);

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE author = 'Author 0042' AND price = 101.42 AND title = 'Bulk Book 10042';

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE author = 'Author 0042'
ORDER BY price, title
LIMIT 20;

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE author = 'Author 0042' AND price BETWEEN 100.00 AND 110.00
ORDER BY price, title
LIMIT 20;

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE price = 10.42 AND title = 'Bulk Book 10042';
