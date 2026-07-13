package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDao {
    private final JdbcExecutor jdbcExecutor;

    public User create(User user) {
        try (Connection connection = jdbcExecutor.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     """
                     INSERT INTO users (username, email, password, amount, restriction, is_admin)
                     VALUES (?, ?, ?, ?, ?, ?)
                     """,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setDouble(4, user.getAmount());
            ps.setBoolean(5, user.isRestriction());
            ps.setBoolean(6, user.isAdmin());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }

            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public Optional<User> findById(long id) {
        return jdbcExecutor.findOne("""
                SELECT id, username, email, password, amount, restriction, is_admin FROM users
                WHERE id = ?""",
                UserDao::mapUser,
                id
        );
    }

    public List<User> findAll() {
        return jdbcExecutor.findMany("""
                SELECT id, username, email, password, amount, restriction, is_admin FROM users
                ORDER BY id""",
                UserDao::mapUser
        );
    }

    public void update(User user) {
        jdbcExecutor.execute("""
                UPDATE users SET username = ?, email = ?, password = ?, amount = ?, restriction = ?, is_admin = ?
                WHERE id = ?""",
                user.getUsername(), user.getEmail(), user.getPassword(), user.getAmount(), user.isRestriction(),
                user.isAdmin(), user.getId()
        );
    }

    public void deleteById(long id) {
        jdbcExecutor.execute("DELETE FROM users WHERE id = ?", id);
    }

    public void resetUserActivity(long userId) {
        jdbcExecutor.execute("DELETE FROM reviews WHERE user_id = ?", userId);
        jdbcExecutor.execute("""
                DELETE FROM order_items
                WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)
                """,
                userId
        );
        jdbcExecutor.execute("DELETE FROM orders WHERE user_id = ?", userId);
    }

    public Optional<User> findByUsername(String username) {
        return jdbcExecutor.findOne("""
            SELECT id, username, email, password, amount, restriction, is_admin
            FROM users
            WHERE username = ?
            """,
                UserDao::mapUser,
                username
        );
    }

    private static User mapUser(ResultSet resultSet) {
        try {
            return User.builder()
                    .id(resultSet.getLong("id"))
                    .username(resultSet.getString("username"))
                    .email(resultSet.getString("email"))
                    .password(resultSet.getString("password"))
                    .amount(resultSet.getDouble("amount"))
                    .restriction(resultSet.getBoolean("restriction"))
                    .isAdmin(resultSet.getBoolean("is_admin"))
                    .build();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map user", e);
        }
    }
}
