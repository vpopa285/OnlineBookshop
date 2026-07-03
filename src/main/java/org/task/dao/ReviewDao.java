package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Review;
import org.task.model.User;

import java.sql.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewDao {
    private final JdbcExecutor jdbcExecutor;

    public Review create(long bookId, Review review) {
        try (Connection connection = jdbcExecutor.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                 INSERT INTO reviews (user_id, book_id, rating, comment)
                 VALUES (?, ?, ?, ?)
                 """,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, review.getUser().getId());
            ps.setLong(2, bookId);
            ps.setInt(3, review.getRate());
            ps.setString(4, review.getComment());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getLong(1));
                }
            }

            return review;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create review", e);
        }
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
            User user = User.builder()
                    .id(resultSet.getLong("id"))
                    .username(resultSet.getString("username"))
                    .email(resultSet.getString("email"))
                    .password(resultSet.getString("password"))
                    .amount(resultSet.getDouble("amount"))
                    .restriction(resultSet.getBoolean("restriction"))
                    .build();

            return Review.builder()
                    .rate(resultSet.getInt("rating"))
                    .comment(resultSet.getString("comment"))
                    .user(user)
                    .build();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map review", e);
        }
    }
}
