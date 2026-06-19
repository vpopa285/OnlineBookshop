package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserDao {
    private final JdbcExecutor jdbcExecutor;

    public void create(User user) {
        jdbcExecutor.execute("INSERT INTO users (id, username, email, password, amount, restriction) VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getAmount(), user.isRestriction()
        );
    }

    public User findById(long id) {
        return jdbcExecutor.findOne("""
                SELECT id, username, email, password, amount, restriction FROM users
                WHERE id = ?""",
                UserDao::mapUser,
                id
        );
    }

    public List<User> findAll() {
        return jdbcExecutor.findMany("""
                SELECT id, username, email, password, amount, restriction FROM users
                ORDER BY id""",
                UserDao::mapUser
        );
    }

    public void update(User user) {
        jdbcExecutor.execute("""
                UPDATE users SET username = ?, email = ?, password = ?, amount = ?, restriction = ?
                WHERE id = ?""",
                user.getUsername(), user.getEmail(), user.getPassword(), user.getAmount(), user.isRestriction(), user.getId()
        );
    }

    public void deleteById(long id) {
        jdbcExecutor.execute("DELETE FROM users WHERE id = ?", id);
    }

    private static User mapUser(ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("username"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getDouble("amount"),
                    resultSet.getBoolean("restriction")
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map user", e);
        }
    }
}
