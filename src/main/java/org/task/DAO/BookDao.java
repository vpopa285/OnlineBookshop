package org.task.DAO;

import org.task.Book;
import org.task.JdbcUtil;
import org.task.Review;
import org.task.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class BookDao {
    private final JdbcUtil jdbcUtil;

    public BookDao(JdbcUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    public void create(Book book) {
        jdbcUtil.execute("INSERT INTO books (id, title, author, genre, content, price) VALUES (?, ?, ?, ?, ?, ?)",
                book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(), book.getContent(), book.getPrice()
        );
    }

    public Book findById(long id) {
        return jdbcUtil.findOne("SELECT id, title, author, genre, content, price FROM books WHERE id = ?",
                BookDao::mapBook,
                id
        );
    }

    public List<Book> findAll() {
        return jdbcUtil.findMany("SELECT id, title, author, genre, content, price FROM books ORDER BY id",
                BookDao::mapBook
        );
    }

    public Book findByIdWithReviews(long id) {
        List<BookReviewRow> rows = jdbcUtil.findMany("""
                       SELECT b.id AS book_id, b.title, b.author, b.genre, b.content, b.price, r.id AS review_id,
                       r.rating, r.comment, u.id AS user_id, u.username, u.email, u.password, u.amount, u.restriction
                       FROM books b
                       LEFT JOIN reviews r ON r.book_id = b.id
                       LEFT JOIN users u ON u.id = r.user_id
                       WHERE b.id = ?
                       ORDER BY r.id""",
                BookDao::mapBookReviewRow,
                id
        );

        if (rows.isEmpty()) {
            return null;
        }

        Book book = rows.get(0).book();
        rows.stream()
                .map(BookReviewRow::review)
                .filter(Objects::nonNull)
                .forEach(book.getReviews()::add);

        return book;
    }

    public void update(Book book) {
        jdbcUtil.execute("""
                UPDATE books SET title = ?, author = ?, genre = ?, content = ?, price = ?
                WHERE id = ?""",
                book.getTitle(), book.getAuthor(), book.getGenre(), book.getContent(), book.getPrice(), book.getId()
        );
    }

    public void deleteById(long id) {
        jdbcUtil.execute("DELETE FROM books WHERE id = ?", id);
    }

    private static Book mapBook(ResultSet resultSet) {
        try {
            return new Book(
                    resultSet.getLong("id"),
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getString("genre"),
                    resultSet.getString("content"),
                    resultSet.getDouble("price")
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map book", e);
        }
    }

    private static BookReviewRow mapBookReviewRow(ResultSet resultSet) {
        try {
            Book book = new Book(
                    resultSet.getLong("book_id"),
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getString("genre"),
                    resultSet.getString("content"),
                    resultSet.getDouble("price")
            );

            resultSet.getLong("review_id");
            if (resultSet.wasNull()) {
                return new BookReviewRow(book, null);
            }

            User user = new User(
                    resultSet.getLong("user_id"),
                    resultSet.getString("username"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getDouble("amount"),
                    resultSet.getBoolean("restriction")
            );

            Review review = new Review(
                    resultSet.getInt("rating"),
                    resultSet.getString("comment"),
                    user
            );

            return new BookReviewRow(book, review);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map book with review", e);
        }
    }

    private record BookReviewRow(Book book, Review review) { }
}
