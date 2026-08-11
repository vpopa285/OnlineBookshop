DELETE FROM reviews;
DELETE FROM books;

ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1;
ALTER TABLE books ALTER COLUMN id RESTART WITH 1;

INSERT INTO books (id, title, author, genre, content, price) VALUES
    (1, 'The Great Gatsby', 'F. Scott Fitzgerald', 'Classic', 'A Jazz Age story.', 12.99),
    (2, 'Clean Code', 'Robert C. Martin', 'Programming', 'Practical programming advice.', 29.99),
    (3, 'Dune', 'Frank Herbert', 'Science Fiction', 'Desert politics and ecology.', 19.99);

INSERT INTO reviews (id, user_id, book_id, rating, comment) VALUES
    (1, 1, 1, 5, 'An absolute masterpiece.'),
    (2, 2, 1, 4, 'Good.'),
    (3, 3, 2, 5, 'Useful.');

ALTER TABLE books ALTER COLUMN id RESTART WITH 4;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 4;
