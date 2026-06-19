package org.task.demo;

import org.task.datasource.HikariCPDataSource;
import org.task.jdbc.JdbcExecutor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public final class IsolationLevelDemo {
    private static final long BOOK_ID = 920_001L;
    private static final long FIRST_USER_ID = 920_001L;
    private static final long SECOND_USER_ID = 920_002L;
    private static final BigDecimal BOOK_PRICE = new BigDecimal("15.00");
    private static final JdbcExecutor JDBC_EXECUTOR = new JdbcExecutor(HikariCPDataSource.create());

    private IsolationLevelDemo() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Case: only one copy of a book is available, but two users buy it concurrently.");

        resetDemoData();
        runRace(Connection.TRANSACTION_READ_COMMITTED, "Default READ_COMMITTED");
        printState("After default isolation");

        resetDemoData();
        runRace(Connection.TRANSACTION_SERIALIZABLE, "Correct SERIALIZABLE isolation");
        printState("After serializable isolation");
    }

    private static void runRace(int isolationLevel, String label) throws InterruptedException {
        CountDownLatch bothTransactionsHaveRead = new CountDownLatch(2);
        CountDownLatch mayUpdate = new CountDownLatch(1);

        Thread first = new Thread(() -> buyLastCopy(
                FIRST_USER_ID, isolationLevel, bothTransactionsHaveRead, mayUpdate), "buyer-1");
        Thread second = new Thread(() -> buyLastCopy(
                SECOND_USER_ID, isolationLevel, bothTransactionsHaveRead, mayUpdate), "buyer-2");

        System.out.println(label + ":");
        first.start();
        second.start();
        bothTransactionsHaveRead.await();
        mayUpdate.countDown();
        first.join();
        second.join();
    }

    private static void buyLastCopy(
            long userId,
            int isolationLevel,
            CountDownLatch bothTransactionsHaveRead,
            CountDownLatch mayUpdate) {
        try (Connection connection = JDBC_EXECUTOR.getConnection()) {
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            int availableCopies = JdbcExecutor.findInt(connection,
                    "SELECT available_copies FROM book_inventory WHERE book_id = ?", BOOK_ID);
            System.out.printf("  user %d read available_copies=%d%n", userId, availableCopies);

            bothTransactionsHaveRead.countDown();
            mayUpdate.await();

            if (availableCopies > 0) {
                JdbcExecutor.execute(connection, """
                        UPDATE book_inventory
                        SET available_copies = 0
                        WHERE book_id = ?""", BOOK_ID);
                JdbcExecutor.insertReturningId(connection, """
                        INSERT INTO orders (user_id, total_price, status)
                        VALUES (?, ?, 'COMPLETED')
                        RETURNING id""", userId, BOOK_PRICE);
            }

            connection.commit();
            System.out.printf("  user %d committed%n", userId);
        } catch (SQLException e) {
            System.out.printf("  user %d rolled back: %s %s%n",
                    userId, e.getSQLState(), e.getMessage().split("\n")[0]);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Isolation demo interrupted", e);
        }
    }

    private static void resetDemoData() throws SQLException {
        try (Connection connection = JDBC_EXECUTOR.getConnection()) {
            JdbcExecutor.executeStatement(connection, """
                    CREATE TABLE IF NOT EXISTS book_inventory (
                        book_id BIGINT PRIMARY KEY REFERENCES books(id),
                        available_copies INT NOT NULL CHECK (available_copies >= 0)
                    )""");
            cleanup(connection);
            insertUser(connection, FIRST_USER_ID, "isolation-buyer-1@example.com");
            insertUser(connection, SECOND_USER_ID, "isolation-buyer-2@example.com");
            JdbcExecutor.execute(connection, "INSERT INTO books (id, title, author, description, price) VALUES (?, 'Serializable Demo Book', 'OnlineBookshop', 'Demo data', ?)",
                    BOOK_ID, BOOK_PRICE);
            JdbcExecutor.execute(connection, "INSERT INTO book_inventory (book_id, available_copies) VALUES (?, 1)", BOOK_ID);
        }
    }

    private static void insertUser(Connection connection, long userId, String email) throws SQLException {
        JdbcExecutor.execute(connection, "INSERT INTO users (id, username, email, password, amount) VALUES (?, ?, ?, 'secret', 100.00)",
                userId, "isolation_user_" + userId, email);
    }

    private static void cleanup(Connection connection) throws SQLException {
        JdbcExecutor.execute(connection,
                "DELETE FROM order_items WHERE order_id IN ( SELECT id FROM orders WHERE user_id IN (?, ?) )", FIRST_USER_ID, SECOND_USER_ID);
        JdbcExecutor.execute(connection, "DELETE FROM orders WHERE user_id IN (?, ?)", FIRST_USER_ID, SECOND_USER_ID);
        JdbcExecutor.execute(connection, "DELETE FROM book_inventory WHERE book_id = ?", BOOK_ID);
        JdbcExecutor.execute(connection, "DELETE FROM users WHERE id IN (?, ?)", FIRST_USER_ID, SECOND_USER_ID);
        JdbcExecutor.execute(connection, "DELETE FROM books WHERE id = ?", BOOK_ID);
    }

    private static void printState(String label) throws SQLException {
        try (Connection connection = JDBC_EXECUTOR.getConnection()) {
            int availableCopies = JdbcExecutor.findInt(connection,
                    "SELECT available_copies FROM book_inventory WHERE book_id = ?", BOOK_ID);
            int completedOrders = JdbcExecutor.count(connection,
                    "SELECT COUNT(*) FROM orders WHERE user_id IN (?, ?) AND status = 'COMPLETED'",
                    FIRST_USER_ID, SECOND_USER_ID);

            System.out.printf("%s: available_copies=%d, completed_orders=%d%n",
                    label, availableCopies, completedOrders);
        }
    }
}
