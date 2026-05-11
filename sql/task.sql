-- At least one full CRUD set of queries for your domain entity
INSERT INTO books (id, title, author, description, price) VALUES (100,'Martin Iden', 'Jack London', 'British clasic literature of 20th century...', 20);
SELECT * FROM books WHERE id = 100;
UPDATE books SET price = 15 WHERE id = 100;
DELETE FROM books WHERE id = 100;

-- Search query with dynamic filters, pagination and sorting
SELECT   id, title, author, price FROM books
WHERE (author =  'George Orwell') AND (price >= 8.00) AND (price <= 15.00)
ORDER BY price
LIMIT 5
    OFFSET 0;

-- Search query with joined data for your use-cases
SELECT b.id, b.title, b.author,  b.price, ROUND(AVG(r.rating), 2) AS avg_rating, COUNT(r.id) AS review_count
FROM books b
         LEFT JOIN reviews r ON r.book_id = b.id
GROUP BY b.id, b.title, b.author, b.price
ORDER BY avg_rating DESC, b.title;

-- Statistic query; can be not related to your use-cases
SELECT b.author, COUNT(b.id) AS book_count, ROUND(AVG(b.price), 2) AS avg_price, MIN(b.price), MAX(b.price),
       ROUND(AVG(r.rating), 2) AS avg_rating, COUNT(r.id) AS total_reviews
FROM books b
         LEFT JOIN reviews r ON r.book_id = b.id
GROUP BY b.author
ORDER BY b.author;

-- Top-something query (for example return authors and number of books they wrote ordered by books count)
SELECT author, COUNT(books) AS books_count FROM books
GROUP BY author
ORDER BY books_count DESC;

-- Cover all your use-cases implemented in OOD module using pure Java
-- Search and Preview covered, as Add, Remove and Read book add Login/Register Review book and See Statistics

-- Buy a book
UPDATE user_wallet SET balance = balance - 23.98 WHERE user_id = 1
                                                   AND balance >= 23.98;

INSERT INTO orders (user_id, total_price, status) VALUES (1, 23.98, 'PENDING')
RETURNING id;

INSERT INTO order_items (order_id, book_id, price) VALUES (currval('orders_id_seq'), 1, 12.99),
                                                          (currval('orders_id_seq'), 2, 10.99);

-- Access History
SELECT b.title, b.author FROM orders o
                                  JOIN users u ON u.id = o.user_id
                                  JOIN order_items oi ON oi.order_id = o.id
                                  JOIN books b ON b.id = oi.book_id
WHERE user_id = 1
ORDER BY o.created_at DESC;

-- Delete comment
INSERT INTO reviews(user_id, book_id, rating, comment) VALUES (1, 1, 1, 'Some bad words');
DELETE FROM reviews WHERE comment LIKE 'Some bad words';

-- Set restrictions for users
UPDATE users SET restrictions = TRUE
WHERE id = 1;
