INSERT INTO users (username, email, password, amount) VALUES
                                                          ('alice_reads',   'alice@example.com',   'hashed_pw_1', 150.00),
                                                          ('bob_pages',     'bob@example.com',     'hashed_pw_2',  80.00),
                                                          ('carol_books',   'carol@example.com',   'hashed_pw_3', 320.00),
                                                          ('dave_literary', 'dave@example.com',    'hashed_pw_4',   0.00),
                                                          ('eva_novels',    'eva@example.com',     'hashed_pw_5', 500.00);

INSERT INTO user_wallet (user_id, balance, currency) VALUES
                                                         (1, 150.00, 'USD'),
                                                         (2,  80.00, 'USD'),
                                                         (3, 320.00, 'USD'),
                                                         (4,   0.00, 'USD'),
                                                         (5, 500.00, 'EUR');

INSERT INTO books (title, author, description, price) VALUES
                                                          ('The Great Gatsby',         'F. Scott Fitzgerald', 'A story of wealth and illusion in the Jazz Age.',        12.99),
                                                          ('To Kill a Mockingbird',    'Harper Lee',          'A tale of racial injustice and childhood innocence.',   10.99),
                                                          ('1984',                     'George Orwell',       'A dystopian vision of a totalitarian future.',          11.99),
                                                          ('Animal Farm',              'George Orwell',       'A political allegory about power and corruption.',       8.99),
                                                          ('Pride and Prejudice',      'Jane Austen',         'A classic romance set in Regency-era England.',          9.99),
                                                          ('Sense and Sensibility',    'Jane Austen',         'Two sisters navigate love and heartbreak.',              9.49),
                                                          ('Brave New World',          'Aldous Huxley',       'A futuristic society built on pleasure and control.',   11.49),
                                                          ('The Catcher in the Rye',   'J.D. Salinger',       'A teenager''s alienation and search for identity.',    10.49),
                                                          ('Fahrenheit 451',           'Ray Bradbury',        'A fireman who burns books discovers their value.',      10.99),
                                                          ('The Hobbit',               'J.R.R. Tolkien',      'A hobbit''s unexpected adventure across Middle-earth.',14.99);

INSERT INTO orders (user_id, total_price, status, created_at) VALUES
                                                                  (1, 23.98, 'COMPLETED',  '2024-01-10 09:15:00'),
                                                                  (1, 10.99, 'COMPLETED',  '2024-02-14 14:30:00'),
                                                                  (2, 11.99, 'COMPLETED',  '2024-02-20 10:00:00'),
                                                                  (3, 35.97, 'COMPLETED',  '2024-03-05 16:45:00'),
                                                                  (3,  8.99, 'CANCELLED',  '2024-03-12 11:20:00'),
                                                                  (5, 49.96, 'COMPLETED',  '2024-04-01 08:00:00'),
                                                                  (2, 14.99, 'PENDING',    '2024-04-18 13:10:00'),
                                                                  (4,  9.99, 'PROCESSING', '2024-04-20 09:55:00');

INSERT INTO order_items (order_id, book_id, price) VALUES
                                                       (1, 1,  12.99),  -- Alice: Gatsby
                                                       (1, 2,  10.99),  -- Alice: Mockingbird
                                                       (2, 2,  10.99),  -- Alice: Mockingbird again
                                                       (3, 3,  11.99),  -- Bob:   1984
                                                       (4, 4,   8.99),  -- Carol: Animal Farm
                                                       (4, 5,   9.99),  -- Carol: Pride & Prejudice
                                                       (4, 6,   9.49),  -- Carol: Sense & Sensibility
                                                       (5, 4,   8.99),  -- Carol: Animal Farm (cancelled)
                                                       (6, 7,  11.49),  -- Eva:   Brave New World
                                                       (6, 8,  10.49),  -- Eva:   Catcher in the Rye
                                                       (6, 9,  10.99),  -- Eva:   Fahrenheit 451
                                                       (6, 10, 14.99),  -- Eva:   The Hobbit
                                                       (7, 10, 14.99),  -- Bob:   The Hobbit (pending)
                                                       (8, 5,   9.99);  -- Dave:  Pride & Prejudice (processing)

INSERT INTO reviews (user_id, book_id, rating, comment) VALUES
                                                            (1, 1, 5, 'An absolute masterpiece. Fitzgerald at his finest.'),
                                                            (1, 2, 5, 'Timeless and deeply moving.'),
                                                            (2, 3, 5, 'Chilling and prophetic. A must-read.'),
                                                            (3, 4, 4, 'Sharp political satire, brilliantly executed.'),
                                                            (3, 5, 5, 'Austen''s wit never gets old.'),
                                                            (3, 6, 4, 'Lovely, though slightly slower than Pride and Prejudice.'),
                                                            (5, 7, 4, 'Unsettling in the best possible way.'),
                                                            (5, 8, 3, 'Interesting but felt a bit repetitive at times.'),
                                                            (5, 9, 5, 'Bradbury''s prose is gorgeous. Loved every page.'),
                                                            (5, 10,5, 'Pure magic from start to finish.'),
                                                            (2, 1, 4, 'Great atmosphere, though the characters felt distant.'),
                                                            (4, 5, 5, 'The best romance novel I have ever read.');
