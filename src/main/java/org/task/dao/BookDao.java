package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.SearchType;
import org.task.model.Book;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Review;
import org.task.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookDao {
    private final JdbcExecutor jdbcExecutor;

    public Book create(Book book) {
        try (Connection connection = jdbcExecutor.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                 INSERT INTO books (title, author, genre, content, price)
                 VALUES (?, ?, ?, ?, ?)
                 """,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getGenre());
            ps.setString(4, book.getContent());
            ps.setDouble(5, book.getPrice());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    book.setId(rs.getLong(1));
                }
            }

            return book;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create book", e);
        }
    }

    public Optional<Book> findById(long id) {
        return jdbcExecutor.findOne("SELECT id, title, author, genre, content, price FROM books WHERE id = ?",
                BookDao::mapBook,
                id
        );
    }

    public List<Book> findAll() {
        return jdbcExecutor.findMany("SELECT id, title, author, genre, content, price FROM books ORDER BY id",
                BookDao::mapBook
        );
    }

    public Optional<Book> findByIdWithReviews(long id) {
        List<BookReviewRow> rows = jdbcExecutor.findMany("""
                   SELECT b.id AS book_id, b.title, b.author, b.genre, b.content, b.price,
                          r.id AS review_id,
                          r.rating, r.comment,
                          u.id AS user_id, u.username, u.email, u.password, u.amount, u.restriction
                   FROM books b
                   LEFT JOIN reviews r ON r.book_id = b.id
                   LEFT JOIN users u ON u.id = r.user_id
                   WHERE b.id = ?
                   ORDER BY r.id
                   """,
                BookDao::mapBookReviewRow,
                id
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Book book = rows.get(0).book();

        rows.stream()
                .map(BookReviewRow::review)
                .filter(Objects::nonNull)
                .forEach(book.getReviews()::add);

        return Optional.of(book);
    }

    public void update(Book book) {
        jdbcExecutor.execute("""
                UPDATE books SET title = ?, author = ?, genre = ?, content = ?, price = ?
                WHERE id = ?""",
                book.getTitle(), book.getAuthor(), book.getGenre(), book.getContent(), book.getPrice(), book.getId()
        );
    }

    public void deleteById(long id) {
        jdbcExecutor.execute("DELETE FROM books WHERE id = ?", id);
    }

    private static Book mapBook(ResultSet resultSet) {
        try {
            return Book.builder()
                    .id(resultSet.getLong("id"))
                    .title(resultSet.getString("title"))
                    .author(resultSet.getString("author"))
                    .genre(resultSet.getString("genre"))
                    .content(resultSet.getString("content"))
                    .price(resultSet.getDouble("price"))
                    .build();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map book", e);
        }
    }

    private static BookReviewRow mapBookReviewRow(ResultSet resultSet) {
        try {
            Book book = Book.builder()
                    .id(resultSet.getLong("book_id"))
                    .title(resultSet.getString("title"))
                    .author(resultSet.getString("author"))
                    .genre(resultSet.getString("genre"))
                    .content(resultSet.getString("content"))
                    .price(resultSet.getDouble("price"))
                    .reviews(new ArrayList<>())
                    .build();

            resultSet.getLong("review_id");
            if (resultSet.wasNull()) {
                return new BookReviewRow(book, null);
            }

            User user = User.builder()
                    .id(resultSet.getLong("user_id"))
                    .username(resultSet.getString("username"))
                    .email(resultSet.getString("email"))
                    .password(resultSet.getString("password"))
                    .amount(resultSet.getDouble("amount"))
                    .restriction(resultSet.getBoolean("restriction"))
                    .build();

            Review review = Review.builder()
                            .rate(resultSet.getInt("rating"))
                            .comment(resultSet.getString("comment"))
                            .user(user)
                            .build();

            return new BookReviewRow(book, review);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map book with review", e);
        }
    }

    public List<Book> search(SearchType searchType, String searchParam) {
        String column = switch (searchType) {
            case TITLE -> "title";
            case AUTHOR -> "author";
            case GENRE -> "genre";
        };

        return jdbcExecutor.findMany(
                "SELECT id, title, author, genre, content, price FROM books WHERE "
                        + column + " LIKE ? ORDER BY title",
                BookDao::mapBook,
                "%" + searchParam + "%"
        );
    }

    public boolean existsByTitle(String title) {
        return jdbcExecutor.findOne("""
            SELECT 1
            FROM books
            WHERE LOWER(title) = LOWER(?)
            LIMIT 1
            """,
                rs -> 1,
                title
        ).isPresent();
    }

    public long count() {
        return jdbcExecutor.findInt("SELECT COUNT(*) FROM books");
    }

    private record BookReviewRow(Book book, Review review) { }
}
