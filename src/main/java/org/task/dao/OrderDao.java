package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Order;
import org.task.model.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderDao {

    private final JdbcExecutor jdbcExecutor;

    public Order create(Order order) {
        try (Connection connection = jdbcExecutor.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                 INSERT INTO orders (user_id)
                 VALUES (?)
                 """,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, order.getUser().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    order.setId(rs.getLong(1));
                }
            }

            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public Optional<Order> findById(long id) {
        return jdbcExecutor.findOne("""
                SELECT o.id,
                       u.id AS user_id,
                       u.username,
                       u.email,
                       u.password,
                       u.amount,
                       u.restriction
                FROM orders o
                JOIN users u ON u.id = o.user_id
                WHERE o.id = ?
                """,
                OrderDao::mapOrder,
                id
        );
    }

    public List<Order> findAllByUserId(long userId) {
        return jdbcExecutor.findMany("""
                SELECT o.id,
                       u.id AS user_id,
                       u.username,
                       u.email,
                       u.password,
                       u.amount,
                       u.restriction
                FROM orders o
                JOIN users u ON u.id = o.user_id
                WHERE u.id = ?
                ORDER BY o.id
                """,
                OrderDao::mapOrder,
                userId
        );
    }

    private static Order mapOrder(ResultSet rs) {
        try {
            User user = User.builder()
                    .id(rs.getLong("user_id"))
                    .username(rs.getString("username"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password"))
                    .amount(rs.getDouble("amount"))
                    .restriction(rs.getBoolean("restriction"))
                    .build();

            return Order.builder()
                    .id(rs.getLong("id"))
                    .user(user)
                    .build();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map order", e);
        }
    }
}
