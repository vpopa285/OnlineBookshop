DROP INDEX IF EXISTS idx_books_author;

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE author = 'Author 0042'
ORDER BY price
LIMIT 20;

CREATE INDEX idx_books_author ON books(author);

EXPLAIN ANALYZE SELECT id, title, author, price
FROM books
WHERE author = 'Author 0042'
ORDER BY price
LIMIT 20;
