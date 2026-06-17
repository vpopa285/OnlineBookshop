package org.task.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.task.util.JdbcUtil;
import org.task.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {
    private final JdbcUtil jdbcUtil;

    @Autowired
    public UserDao(JdbcUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    public void create(User user) {
        jdbcUtil.execute("INSERT INTO users (id, username, email, password, amount, restriction) VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getAmount(), user.isRestriction()
        );
    }

    public User findById(long id) {
        return jdbcUtil.findOne("""
                SELECT id, username, email, password, amount, restriction FROM users
                WHERE id = ?""",
                UserDao::mapUser,
                id
        );
    }

    public List<User> findAll() {
        return jdbcUtil.findMany("""
                SELECT id, username, email, password, amount, restriction FROM users
                ORDER BY id""",
                UserDao::mapUser
        );
    }

    public void update(User user) {
        jdbcUtil.execute("""
                UPDATE users SET username = ?, email = ?, password = ?, amount = ?, restriction = ?
                WHERE id = ?""",
                user.getUsername(), user.getEmail(), user.getPassword(), user.getAmount(), user.isRestriction(), user.getId()
        );
    }

    public void deleteById(long id) {
        jdbcUtil.execute("DELETE FROM users WHERE id = ?", id);
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
