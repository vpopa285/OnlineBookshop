package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Review;
import org.task.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewDao {
    private final JdbcExecutor jdbcExecutor;

    public void create(long bookId, Review review) {
        jdbcExecutor.execute("INSERT INTO reviews (user_id, book_id, rating, comment) VALUES (?, ?, ?, ?)",
                review.user().getId(), bookId, review.rate(), review.comment()
        );
    }

    public List<Review> findByBookId(long bookId) {
        return jdbcExecutor.findMany("""
                SELECT r.rating, r.comment, u.id, u.username, u.email, u.password, u.amount, u.restriction
                FROM reviews r
                JOIN users u ON u.id = r.user_id
                WHERE r.book_id = ?
                ORDER BY r.id""",
                ReviewDao::mapReview,
                bookId
        );
    }

    public void deleteByUserAndBookId(long userId, long bookId) {
        jdbcExecutor.execute("DELETE FROM reviews WHERE user_id = ? AND book_id = ?",
                userId,
                bookId
        );
    }

    private static Review mapReview(ResultSet resultSet) {
        try {
            User user = new User(
                    resultSet.getLong("id"),
                    resultSet.getString("username"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getDouble("amount"),
                    resultSet.getBoolean("restriction")
            );

            return new Review(
                    resultSet.getInt("rating"),
                    resultSet.getString("comment"),
                    user
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map review", e);
        }
    }
}
