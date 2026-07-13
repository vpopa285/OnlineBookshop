RUNSCRIPT FROM 'classpath:db/initSchema.sql';

INSERT INTO users (id, username, email, password, amount, restriction) VALUES
    (1, 'alice_reads', 'alice_reads@example.com', 'hashed_pw_1', 150.00, FALSE),
    (2, 'bob_pages', 'bob@example.com', 'hashed_pw_2', 80.00, FALSE),
    (3, 'restricted_user', 'restricted@example.com', 'hashed_pw_3', 0.00, TRUE);

INSERT INTO books (id, title, author, genre, content, price) VALUES
    (1, 'The Great Gatsby', 'F. Scott Fitzgerald', 'Classic', 'A Jazz Age story.', 12.99),
    (2, '1984', 'George Orwell', 'Dystopian', 'A totalitarian future.', 11.99),
    (3, 'Clean Code', 'Robert C. Martin', 'Programming', 'Software craftsmanship.', 30.00);

INSERT INTO reviews (user_id, book_id, rating, comment) VALUES
    (1, 1, 5, 'An absolute masterpiece.'),
    (2, 1, 4, 'Great atmosphere.'),
    (2, 2, 5, 'Chilling and prophetic.');

ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE books ALTER COLUMN id RESTART WITH 4;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 4;
