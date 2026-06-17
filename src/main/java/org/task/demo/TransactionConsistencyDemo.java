package org.task.demo;

import org.task.datasource.HikariCPDataSource;
import org.task.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionConsistencyDemo {
    private static final long USER_ID = 910_001L;
    private static final long BOOK_ID = 910_001L;
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("50.00");
    private static final BigDecimal BOOK_PRICE = new BigDecimal("25.00");
    private static final JdbcUtil JDBC_UTIL = new JdbcUtil(HikariCPDataSource.create());

    private TransactionConsistencyDemo() {
    }

    public static void main(String[] args) throws SQLException {
        resetDemoData();
        printState("Initial state");
        runBrokenPurchaseWithoutTransaction();
        printState("After failed purchase without transaction");

        resetDemoData();
        runBrokenPurchaseWithTransaction();
        printState("After failed purchase with transaction");
    }

    private static void runBrokenPurchaseWithoutTransaction() {
        try (Connection connection = JDBC_UTIL.getConnection()) {
            debitWallet(connection);
            long orderId = createOrder(connection);
            createBrokenOrderItem(connection, orderId);
        } catch (SQLException e) {
            System.out.println("Failure without transaction: " + e.getSQLState() + " "
                    + e.getMessage().split("\n")[0]);
        }
    }

    private static void runBrokenPurchaseWithTransaction() {
        try (Connection connection = JDBC_UTIL.getConnection()) {
            connection.setAutoCommit(false);

            try {
                debitWallet(connection);
                long orderId = createOrder(connection);
                createBrokenOrderItem(connection, orderId);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failure with transaction: " + e.getSQLState() + " "
                        + e.getMessage().split("\n")[0]);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Transaction demo failed", e);
        }
    }

    private static void debitWallet(Connection connection) throws SQLException {
        JdbcUtil.execute(connection, "UPDATE user_wallet SET balance = balance - ? WHERE user_id = ? AND balance >= ?",
                BOOK_PRICE, USER_ID, BOOK_PRICE);
    }

    private static long createOrder(Connection connection) throws SQLException {
        return JdbcUtil.insertReturningId(connection, "INSERT INTO orders (user_id, total_price, status) VALUES (?, ?, 'PENDING') RETURNING id",
                USER_ID, BOOK_PRICE);
    }

    private static void createBrokenOrderItem(Connection connection, long orderId) throws SQLException {
        JdbcUtil.execute(connection, "INSERT INTO order_items (order_id, book_id, price) VALUES (?, ?, ?)",
                orderId, -BOOK_ID, BOOK_PRICE);
    }

    private static void resetDemoData() throws SQLException {
        try (Connection connection = JDBC_UTIL.getConnection()) {
            cleanup(connection);
            JdbcUtil.execute(connection,
                    " INSERT INTO users (id, username, email, password, amount) VALUES (?, 'tx_demo_user', 'tx-demo@example.com', 'secret', ?)",
                    USER_ID, INITIAL_BALANCE);
            JdbcUtil.execute(connection, "INSERT INTO user_wallet (user_id, balance, currency) VALUES (?, ?, 'USD')",
                    USER_ID, INITIAL_BALANCE);
            JdbcUtil.execute(connection,
                    "INSERT INTO books (id, title, author, description, price) VALUES (?, 'Transaction Demo Book', 'OnlineBookshop', 'Demo data', ?)",
                    BOOK_ID, BOOK_PRICE);
        }
    }

    private static void cleanup(Connection connection) throws SQLException {
        JdbcUtil.execute(connection, """
                DELETE FROM order_items
                WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)""", USER_ID);
        JdbcUtil.execute(connection, "DELETE FROM orders WHERE user_id = ?", USER_ID);
        JdbcUtil.execute(connection, "DELETE FROM user_wallet WHERE user_id = ?", USER_ID);
        JdbcUtil.execute(connection, "DELETE FROM users WHERE id = ?", USER_ID);
        JdbcUtil.execute(connection, "DELETE FROM books WHERE id = ?", BOOK_ID);
    }

    private static void printState(String label) throws SQLException {
        try (Connection connection = JDBC_UTIL.getConnection()) {
            BigDecimal balance = JdbcUtil.findMoney(connection,
                    "SELECT balance FROM user_wallet WHERE user_id = ?", USER_ID);
            int orders = JdbcUtil.count(connection,
                    "SELECT COUNT(*) FROM orders WHERE user_id = ?", USER_ID);
            int items = JdbcUtil.count(connection, """
                    SELECT COUNT(*)
                    FROM order_items oi
                    JOIN orders o ON o.id = oi.order_id
                    WHERE o.user_id = ?""", USER_ID);

            System.out.printf("%s: wallet=%s, orders=%d, order_items=%d%n",
                    label, balance, orders, items);
        }
    }
}
