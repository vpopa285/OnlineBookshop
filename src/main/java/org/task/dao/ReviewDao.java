package org.task.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.task.util.JdbcUtil;
import org.task.model.Review;
import org.task.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ReviewDao {
    private final JdbcUtil jdbcUtil;

    @Autowired
    public ReviewDao(JdbcUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    public void create(long bookId, Review review) {
        jdbcUtil.execute("INSERT INTO reviews (user_id, book_id, rating, comment) VALUES (?, ?, ?, ?)",
                review.user().getId(), bookId, review.rate(), review.comment()
        );
    }

    public List<Review> findByBookId(long bookId) {
        return jdbcUtil.findMany("""
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
        jdbcUtil.execute("DELETE FROM reviews WHERE user_id = ? AND book_id = ?",
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
