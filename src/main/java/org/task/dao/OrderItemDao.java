package org.task.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Book;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.sql.PreparedStatement;

@Repository
@RequiredArgsConstructor
public class OrderItemDao {

    private final JdbcExecutor jdbcExecutor;

    public OrderItem create(OrderItem item) {
        try (Connection connection = jdbcExecutor.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                 INSERT INTO order_items (order_id, book_id)
                 VALUES (?, ?)
                 """,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, item.getOrder().getId());
            ps.setLong(2, item.getBook().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }

            return item;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order item", e);
        }
    }

    public Optional<OrderItem> findById(long id) {
        return jdbcExecutor.findOne("""
                SELECT oi.id,
                       o.id AS order_id,

                       u.id AS user_id,
                       u.username,
                       u.email,
                       u.password,
                       u.amount,
                       u.restriction,

                       b.id AS book_id,
                       b.title,
                       b.author,
                       b.genre,
                       b.content,
                       b.price

                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN users u ON u.id = o.user_id
                JOIN books b ON b.id = oi.book_id

                WHERE oi.id = ?
                """,
                OrderItemDao::mapOrderItem,
                id
        );
    }

    public List<OrderItem> findAllByOrderId(long orderId) {
        return jdbcExecutor.findMany("""
                SELECT oi.id,
                       o.id AS order_id,

                       u.id AS user_id,
                       u.username,
                       u.email,
                       u.password,
                       u.amount,
                       u.restriction,

                       b.id AS book_id,
                       b.title,
                       b.author,
                       b.genre,
                       b.content,
                       b.price

                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN users u ON u.id = o.user_id
                JOIN books b ON b.id = oi.book_id

                WHERE o.id = ?
                ORDER BY oi.id
                """,
                OrderItemDao::mapOrderItem,
                orderId
        );
    }


    private static OrderItem mapOrderItem(ResultSet rs) {
        try {

            User user = User.builder()
                    .id(rs.getLong("user_id"))
                    .username(rs.getString("username"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password"))
                    .amount(rs.getDouble("amount"))
                    .restriction(rs.getBoolean("restriction"))
                    .build();

            Order order = Order.builder()
                    .id(rs.getLong("order_id"))
                    .user(user)
                    .build();

            Book book = Book.builder()
                    .id(rs.getLong("book_id"))
                    .title(rs.getString("title"))
                    .author(rs.getString("author"))
                    .genre(rs.getString("genre"))
                    .content(rs.getString("content"))
                    .price(rs.getDouble("price"))
                    .build();

            return OrderItem.builder()
                    .id(rs.getLong("id"))
                    .order(order)
                    .book(book)
                    .build();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map order item", e);
        }
    }
}
