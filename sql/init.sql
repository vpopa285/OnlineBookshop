CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    amount NUMERIC(10,2) DEFAULT 0,
    restriction BOOLEAN DEFAULT FALSE,
    is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance NUMERIC(10,2) DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    CONSTRAINT fk_user_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    price NUMERIC(10,2) NOT NULL
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_price NUMERIC(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
);
